package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RadagastWizardOfWilds.class, ColossalDreadmaw.class, Shock.class})
class RadagastWizardOfWildsTest extends BaseCardTest {

    private static final String BEAST_MODE = "Create a 3/3 green Beast creature token";
    private static final String BIRD_MODE = "Create a 2/2 blue Bird creature token with flying";

    @Test
    void createsABeastTokenWhenCastingASpellWithManaValueFiveOrGreater() {
        addRadagast();

        castColossalDreadmaw(BEAST_MODE);

        Permanent beast = findPermanent(player1, "Beast");
        assertThat(beast.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(beast.getCard().getSubtypes()).contains(CardSubtype.BEAST);
        assertThat(beast.getEffectivePower()).isEqualTo(3);
        assertThat(beast.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    void createsAFlyingBirdTokenWhenTheBirdModeIsChosen() {
        addRadagast();

        castColossalDreadmaw(BIRD_MODE);

        Permanent bird = findPermanent(player1, "Bird");
        assertThat(bird.getCard().getColor()).isEqualTo(CardColor.BLUE);
        assertThat(bird.getCard().getSubtypes()).contains(CardSubtype.BIRD);
        assertThat(gqs.hasKeyword(gd, bird, Keyword.FLYING)).isTrue();
        assertThat(bird.getEffectivePower()).isEqualTo(2);
        assertThat(bird.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void radagastHasWardOne() {
        Permanent radagast = addRadagast();

        castShockAt(player2, radagast);
        harness.handleMayAbilityChosen(player2, false);

        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    void beastsAndBirdsYouControlHaveWardOne() {
        addRadagast();
        castColossalDreadmaw(BEAST_MODE);
        Permanent beast = findPermanent(player1, "Beast");

        castShockAt(player2, beast);
        harness.handleMayAbilityChosen(player2, false);

        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    void birdsYouControlHaveWardOne() {
        addRadagast();
        castColossalDreadmaw(BIRD_MODE);
        Permanent bird = findPermanent(player1, "Bird");

        castShockAt(player2, bird);
        harness.handleMayAbilityChosen(player2, false);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Bird");
        assertThat(bird.getMarkedDamage()).isZero();
    }

    @Test
    void payingWardAllowsTheSpellToResolve() {
        addRadagast();
        castColossalDreadmaw(BIRD_MODE);
        Permanent bird = findPermanent(player1, "Bird");

        castShockAt(player2, bird);
        harness.handleMayAbilityChosen(player2, true);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Bird");
    }

    @Test
    void yourOwnSpellsDoNotTriggerWard() {
        addRadagast();
        castColossalDreadmaw(BIRD_MODE);
        Permanent bird = findPermanent(player1, "Bird");

        castShockAt(player1, bird);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Bird");
    }

    @Test
    void unrelatedCreaturesDoNotGainWard() {
        addRadagast();
        castColossalDreadmaw(BEAST_MODE);
        Permanent dinosaur = findPermanent(player1, "Colossal Dreadmaw");

        castShockAt(player2, dinosaur);
        resolveAllTriggers();

        assertThat(dinosaur.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void spellsBelowManaValueFiveDoNotCreateTokens() {
        addRadagast();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Beast")).isZero();
        assertThat(countPermanents(player1, "Bird")).isZero();
        harness.assertLife(player2, 18);
    }

    @Test
    void opponentsSpellsDoNotCreateTokens() {
        addRadagast();
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new ColossalDreadmaw(), "{4}{G}{G}");
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Beast")).isZero();
        assertThat(countPermanents(player1, "Bird")).isZero();
        harness.assertOnBattlefield(player2, "Colossal Dreadmaw");
    }

    private Permanent addRadagast() {
        Permanent radagast = addCreatureReady(player1, new RadagastWizardOfWilds());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return radagast;
    }

    private void castColossalDreadmaw(String mode) {
        harness.castFromHand(player1, new ColossalDreadmaw(), "{4}{G}{G}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, mode);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void castShockAt(Player player, Permanent target) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player, List.of(new Shock()));
        harness.addMana(player, ManaColor.RED, 1);
        harness.addMana(player, ManaColor.COLORLESS, 1);
        harness.castInstant(player, 0, target.getId());
        harness.passBothPriorities();
    }
}
