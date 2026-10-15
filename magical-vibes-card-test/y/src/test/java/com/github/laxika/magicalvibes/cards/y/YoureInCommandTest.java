package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.b.BriselaVoiceOfNightmares;
import com.github.laxika.magicalvibes.cards.b.BrunaTheFadingLight;
import com.github.laxika.magicalvibes.cards.c.Clone;
import com.github.laxika.magicalvibes.cards.g.GiselaTheBrokenBlade;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({YoureInCommand.class, GrizzlyBears.class, Clone.class,
        BriselaVoiceOfNightmares.class, BrunaTheFadingLight.class, GiselaTheBrokenBlade.class})
class YoureInCommandTest extends BaseCardTest {

    @Test
    @DisplayName("Makes the target creature the only commander and starts its tax at zero")
    void makesTargetTheOnlyCommander() {
        Card previousCommander = new GrizzlyBears();
        gd.playerCommanders.put(player1.getId(), new ArrayList<>(List.of(previousCommander)));
        gd.commanderTaxByCardId.put(previousCommander.getId(), 3);
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        castYoureInCommand(target);

        assertThat(gd.playerCommanders.get(player1.getId())).containsExactly(target.getCard());
        assertThat(gd.commanderTaxByCardId).containsEntry(target.getCard().getId(), 0);
        assertThat(gd.commanderTaxByCardId).doesNotContainKey(previousCommander.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
    }

    @Test
    @DisplayName("Cannot target an opponent's creature")
    void cannotTargetOpponentsCreature() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new YoureInCommand()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you own and control");
    }

    @Test
    @DisplayName("Cannot target a creature the player does not own")
    void cannotTargetUnownedCreature() {
        Card targetCard = new GrizzlyBears();
        targetCard.setOwnerId(player2.getId());
        Permanent target = new Permanent(targetCard);
        target.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(target);
        harness.setHand(player1, List.of(new YoureInCommand()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you own and control");
    }

    private void castYoureInCommand(Permanent target) {
        harness.setHand(player1, List.of(new YoureInCommand()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveSorcery(player1, 0, target.getId());
    }

    @Test
    void preservesFormerCommanderInCommandZone() {
        Card formerCommander = new GrizzlyBears();
        gd.playerCommanders.put(player1.getId(), new ArrayList<>(List.of(formerCommander)));
        gd.playerCommandZones.computeIfAbsent(player1.getId(), id -> new ArrayList<>()).add(formerCommander);
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        castYoureInCommand(target);

        assertThat(gd.playerCommandZones.get(player1.getId())).containsExactly(formerCommander);
        assertThat(gd.isCommander(formerCommander.getId())).isFalse();
        assertThat(gd.isCommander(target.getOriginalCard().getId())).isTrue();
    }

    @Test
    void targetLeavingBattlefieldPreservesExistingCommander() {
        Card formerCommander = new GrizzlyBears();
        gd.playerCommanders.put(player1.getId(), new ArrayList<>(List.of(formerCommander)));
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new YoureInCommand()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castSorcery(player1, 0, target.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToExile(gd, target));

        harness.passBothPriorities();

        assertThat(gd.playerCommanders.get(player1.getId())).containsExactly(formerCommander);
    }

    @Test
    @CardUsed({YoureInCommand.class, GrizzlyBears.class, Clone.class})
    void copiedCreatureDesignatesThePhysicalCard() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        Card cloneCard = new Clone();
        harness.castFromHand(player1, cloneCard, "{3}{U}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bears.getId());
        Permanent clone = gd.playerBattlefields.get(player1.getId()).getFirst();

        castYoureInCommand(clone);

        assertThat(gd.playerCommanders.get(player1.getId())).containsExactly(cloneCard);
        assertThat(gd.isCommander(clone.getOriginalCard().getId())).isTrue();
    }

    @Test
    @CardUsed({YoureInCommand.class, BriselaVoiceOfNightmares.class,
            BrunaTheFadingLight.class, GiselaTheBrokenBlade.class})
    void meldedCreatureDesignatesBothPhysicalCards() {
        Card bruna = new BrunaTheFadingLight();
        Card gisela = new GiselaTheBrokenBlade();
        Permanent brisela = addCreatureReady(player1, new BriselaVoiceOfNightmares());
        brisela.getMeldComponentCards().addAll(List.of(bruna, gisela));

        castYoureInCommand(brisela);

        assertThat(gd.playerCommanders.get(player1.getId())).containsExactlyInAnyOrder(bruna, gisela);
        assertThat(gd.commanderTaxByCardId).containsEntry(bruna.getId(), 0).containsEntry(gisela.getId(), 0);
    }

    @Test
    void commanderDamageCausesLossOutsideCommanderFormat() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        castYoureInCommand(target);
        harness.setLife(player2, 100);
        gd.commanderDamageReceived.put(player2.getId(),
                new HashMap<>(java.util.Map.of(target.getOriginalCard().getId(), 20)));
        target.setAttacking(true);
        harness.forceActivePlayer(player1);

        harness.resolveCombatDamage();
        harness.runStateBasedActions();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }
}
