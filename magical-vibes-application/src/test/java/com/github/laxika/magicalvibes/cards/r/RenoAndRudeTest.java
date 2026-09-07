package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RenoAndRude.class, Forest.class, GrizzlyBears.class, Ornithopter.class})
class RenoAndRudeTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage exiles the damaged player's top card but does not grant permission before sacrificing")
    void declineSacrificeLeavesCardExiledWithoutPermission() {
        Permanent reno = addAttackingReno();
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player2, List.of(topCard, new Forest()));

        resolveCombatAndTrigger();

        ExiledCardEntry exiled = gd.findExiledCard(topCard.getId());
        assertThat(exiled).isNotNull();
        assertThat(exiled.sourcePermanentId()).isEqualTo(reno.getId());
        assertThat(gd.exilePlayPermissions).doesNotContainKey(topCard.getId());

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.exilePlayPermissions).doesNotContainKey(topCard.getId());
        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
    }

    @Test
    @DisplayName("Sacrificing another artifact grants end-of-turn play permission with any-color mana")
    void sacrificingArtifactGrantsPlayPermission() {
        addAttackingReno();
        Permanent artifact = addCreatureReady(player1, new Ornithopter());
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player2, List.of(topCard));

        resolveCombatAndTrigger();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(topCard.getId());
        assertThat(gd.exilePlayAnyManaType).contains(topCard.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(artifact.getCard());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castFromExile(player1, topCard.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.findExiledCard(topCard.getId())).isNull();
    }

    @Test
    @DisplayName("The sacrificed branch lets the controller play an exiled land")
    void sacrificingCreatureLetsControllerPlayExiledLand() {
        addAttackingReno();
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Card topCard = new Forest();
        harness.setLibrary(player2, List.of(topCard));

        resolveCombatAndTrigger();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromExile(player1, topCard.getId());

        assertThat(gd.findExiledCard(topCard.getId())).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(topCard.getId()));
    }

    private Permanent addAttackingReno() {
        Permanent reno = addCreatureReady(player1, new RenoAndRude());
        reno.setAttacking(true);
        return reno;
    }

    private void resolveCombatAndTrigger() {
        resolveCombat();
        harness.passBothPriorities();
    }
}
