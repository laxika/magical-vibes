package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TombBlade.class, GrizzlyBears.class})
class TombBladeTest extends BaseCardTest {

    @Test
    @DisplayName("The damaged player loses life equal to their creature count when they decline")
    void declinedAbilityLosesLifeEqualToCreatureCount() {
        Permanent blade = addCreatureReady(player1, new TombBlade());
        blade.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.getLife(player2.getId())).isEqualTo(13);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The damaged player may sacrifice a creature instead of losing life")
    void sacrificesCreatureInsteadOfLosingLife() {
        Permanent blade = addCreatureReady(player1, new TombBlade());
        blade.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());

        resolveCombat();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.getLife(player2.getId())).isEqualTo(15);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Unearth returns Tomb Blade with haste and exiles it at the next end step")
    void unearthReturnsWithHasteAndExilesAtEndStep() {
        harness.setGraveyard(player1, List.of(new TombBlade()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Tomb Blade").getGrantedKeywords()).contains(Keyword.HASTE);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Tomb Blade");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(cardInExile -> cardInExile.getName().equals("Tomb Blade"));
    }

    @Test
    @DisplayName("The damaged player chooses which creature to sacrifice")
    void damagedPlayerChoosesCreature() {
        Permanent blade = addCreatureReady(player1, new TombBlade());
        blade.setAttacking(true);
        Permanent first = addCreatureReady(player2, new TombBlade());
        Permanent second = addCreatureReady(player2, new TombBlade());

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.handlePermanentChosen(player2, second.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(15);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(first).doesNotContain(second);
        harness.assertInGraveyard(player2, "Tomb Blade");
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A damaged player with no creatures loses no additional life")
    void noCreaturesMeansNoAdditionalLifeLoss() {
        Permanent blade = addCreatureReady(player1, new TombBlade());
        blade.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(15);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Tomb Blade");
    }

    @Test
    @DisplayName("Creature count is evaluated when the trigger resolves")
    void countsCreaturesAtResolution() {
        Permanent blade = addCreatureReady(player1, new TombBlade());
        blade.setAttacking(true);
        addCreatureReady(player2, new TombBlade());

        resolveCombat();
        addCreatureReady(player2, new TombBlade());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.getLife(player2.getId())).isEqualTo(13);
        assertThat(countPermanents(player2, "Tomb Blade")).isEqualTo(2);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Sacrificing an unearthed Tomb Blade exiles it and pays the sacrifice")
    void sacrificingUnearthedBladeExilesIt() {
        harness.setGraveyard(player1, List.of(new TombBlade()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        Permanent attacker = addCreatureReady(player2, new TombBlade());
        attacker.setAttacking(true);

        resolveCombat(player2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player1.getId())).isEqualTo(15);
        harness.assertNotOnBattlefield(player1, "Tomb Blade");
        harness.assertNotInGraveyard(player1, "Tomb Blade");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Tomb Blade"));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
