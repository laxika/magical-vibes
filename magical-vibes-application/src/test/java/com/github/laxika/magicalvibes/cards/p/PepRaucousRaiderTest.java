package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AvariceTotem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PepRaucousRaider.class, GrizzlyBears.class, AvariceTotem.class})
class PepRaucousRaiderTest extends BaseCardTest {

    @Test
    @DisplayName("A creature's combat damage exiles the damaged player's top card for play this turn")
    void combatDamageExilesTopCardWithPlayPermission() {
        addPep();
        addAttacker();
        Card topCard = new GrizzlyBears();
        topCard.setOwnerId(player2.getId());
        harness.setLibrary(player2, List.of(topCard));

        resolveCombatAndTrigger();

        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(topCard.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(topCard.getId());
        assertThat(gd.exilePlayAnyManaType).doesNotContain(topCard.getId());
    }

    @Test
    @DisplayName("A nonland permanent played from exile becomes an artifact with Pep's mana ability")
    void nonlandPermanentBecomesArtifactAndGainsManaAbility() {
        addPep();
        addAttacker();
        harness.addToBattlefield(player1, new AvariceTotem());
        Card topCard = new GrizzlyBears();
        topCard.setOwnerId(player2.getId());
        harness.setLibrary(player2, List.of(topCard));

        resolveCombatAndTrigger();
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castFromExile(player1, topCard.getId());
        harness.passBothPriorities();

        Permanent artifactCreature = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(topCard.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(gqs.isArtifact(gd, artifactCreature)).isTrue();

        artifactCreature.setSummoningSick(false);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(artifactCreature), null, null);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)
                .validIds()).contains(artifactCreature.getId());
        harness.handlePermanentChosen(player1, artifactCreature.getId());
        harness.handleListChoice(player1, ManaColor.RED.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(3);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getId().equals(topCard.getId()));
    }

    private Permanent addPep() {
        return addCreatureReady(player1, new PepRaucousRaider());
    }

    private Permanent addAttacker() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        return attacker;
    }

    private void resolveCombatAndTrigger() {
        resolveCombat();
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
    }
}
