package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.d.DirectCurrent;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.r.RadicalIdea;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LeagueGuildmage.class, CounselOfTheSoratami.class, GrizzlyBears.class, Island.class,
        RadicalIdea.class, DirectCurrent.class})
class LeagueGuildmageTest extends BaseCardTest {

    @Test
    @DisplayName("Draw ability draws a card")
    void drawAbilityDrawsCard() {
        addReadyGuildmage(player1);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Island()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Copy ability copies an instant or sorcery spell with matching mana value")
    void copyAbilityCopiesSpellWithMatchingManaValue() {
        addReadyGuildmage(player1);

        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(counsel));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0, 0);
        harness.activateAbility(player1, 0, 1, 3, counsel.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).filteredOn(StackEntry::isCopy).hasSize(1);
        assertThat(gd.stack).filteredOn(StackEntry::isCopy)
                .allMatch(copy -> copy.getControllerId().equals(player1.getId()));
    }

    @Test
    @DisplayName("Copy ability requires the target spell's mana value to equal X")
    void copyAbilityRequiresMatchingX() {
        addReadyGuildmage(player1);

        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(counsel));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, 1, counsel.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Copy ability cannot target a creature spell")
    void copyAbilityCannotTargetCreatureSpell() {
        addReadyGuildmage(player1);

        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);

        UUID bearsId = bears.getId();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, 2, bearsId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Copy ability cannot target an opponent's spell")
    void copyAbilityCannotTargetOpponentSpell() {
        addReadyGuildmage(player1);

        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player2, List.of(counsel));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.forceActivePlayer(player2);
        harness.castSorcery(player2, 0, 0);

        UUID counselId = counsel.getId();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, 3, counselId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void copyAndOriginalInstantResolveIndependently() {
        addReadyGuildmage(player1);
        RadicalIdea idea = new RadicalIdea();
        harness.setHand(player1, List.of(idea));
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0);
        harness.activateAbility(player1, 0, 1, 2, idea.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(idea);
    }

    @Test
    void copyMayKeepOriginalTarget() {
        addReadyGuildmage(player1);
        DirectCurrent current = new DirectCurrent();
        harness.setHand(player1, List.of(current));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0, player2.getId());
        harness.activateAbility(player1, 0, 1, 3, current.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
        harness.assertLife(player1, 20);
    }

    @Test
    void copyMayChooseDifferentTarget() {
        addReadyGuildmage(player1);
        DirectCurrent current = new DirectCurrent();
        harness.setHand(player1, List.of(current));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0, player2.getId());
        harness.activateAbility(player1, 0, 1, 3, current.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
    }

    @Test
    void drawAbilityTapsGuildmageAsCost() {
        addReadyGuildmage(player1);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void summoningSickGuildmageCannotDraw() {
        Permanent guildmage = harness.addToBattlefieldAndReturn(player1, new LeagueGuildmage());
        guildmage.setSummoningSick(true);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addReadyGuildmage(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new LeagueGuildmage());
        permanent.setSummoningSick(false);
    }
}
