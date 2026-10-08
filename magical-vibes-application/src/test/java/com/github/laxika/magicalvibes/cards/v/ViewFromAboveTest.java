package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.m.ManaCylix;
import com.github.laxika.magicalvibes.cards.m.MarkOfAsylum;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.cards.z.ZombieOutlander;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ViewFromAbove.class, ZombieOutlander.class, VedalkenOutlander.class,
        ManaCylix.class, MarkOfAsylum.class, Unsummon.class})
class ViewFromAboveTest extends BaseCardTest {

    private UUID castOnOutlander() {
        harness.addToBattlefield(player1, new ZombieOutlander());
        harness.setHand(player1, List.of(new ViewFromAbove()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        UUID target = harness.getPermanentId(player1, "Zombie Outlander");
        harness.castInstant(player1, 0, target);
        return target;
    }

    @Test
    @DisplayName("Target creature gains flying until end of turn")
    void grantsFlying() {
        castOnOutlander();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Zombie Outlander").getGrantedKeywords()).contains(Keyword.FLYING);
    }

    @Test
    @DisplayName("Flying wears off at end of turn")
    void flyingWearsOff() {
        castOnOutlander();
        harness.passBothPriorities();
        assertThat(findPermanent(player1, "Zombie Outlander").getGrantedKeywords()).contains(Keyword.FLYING);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Zombie Outlander").getGrantedKeywords()).doesNotContain(Keyword.FLYING);
    }

    @Test
    @DisplayName("Without a white permanent, the spell goes to the graveyard")
    void noWhitePermanentGoesToGraveyard() {
        castOnOutlander();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "View from Above");
    }

    @Test
    @DisplayName("Controlling a white permanent returns the spell to its owner's hand")
    void whitePermanentReturnsToHand() {
        harness.addToBattlefield(player1, new VedalkenOutlander());
        castOnOutlander();
        harness.passBothPriorities();

        // Flying still resolved on the target.
        assertThat(findPermanent(player1, "Zombie Outlander").getGrantedKeywords()).contains(Keyword.FLYING);
        // Spell bounced off the stack back to hand rather than the graveyard.
        harness.assertInHand(player1, "View from Above");
        harness.assertNotInGraveyard(player1, "View from Above");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        harness.addToBattlefield(player1, new ManaCylix());
        harness.setHand(player1, List.of(new ViewFromAbove()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        UUID relic = harness.getPermanentId(player1, "Mana Cylix");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, relic))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An opponent's white permanent does not return the spell")
    void opponentsWhitePermanentDoesNotReturnSpell() {
        harness.addToBattlefield(player2, new VedalkenOutlander());
        castOnOutlander();
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "View from Above");
        harness.assertInGraveyard(player1, "View from Above");
        assertThat(findPermanent(player1, "Zombie Outlander").getGrantedKeywords()).contains(Keyword.FLYING);
    }

    @Test
    @DisplayName("A white card in hand does not satisfy the permanent condition")
    void whiteCardInHandDoesNotReturnSpell() {
        castOnOutlander();
        harness.setHand(player1, List.of(new VedalkenOutlander()));
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "View from Above");
        harness.assertInGraveyard(player1, "View from Above");
    }

    @Test
    @DisplayName("The targeted white creature can itself satisfy the condition")
    void whiteTargetReturnsSpell() {
        harness.addToBattlefield(player1, new VedalkenOutlander());
        harness.setHand(player1, List.of(new ViewFromAbove()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Vedalken Outlander"));

        assertThat(findPermanent(player1, "Vedalken Outlander").getGrantedKeywords()).contains(Keyword.FLYING);
        harness.assertInHand(player1, "View from Above");
        harness.assertNotInGraveyard(player1, "View from Above");
    }

    @Test
    @DisplayName("An opponent's creature can receive flying")
    void canTargetOpponentsCreature() {
        harness.addToBattlefield(player2, new ZombieOutlander());
        harness.setHand(player1, List.of(new ViewFromAbove()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Zombie Outlander"));

        assertThat(findPermanent(player2, "Zombie Outlander").getGrantedKeywords()).contains(Keyword.FLYING);
        harness.assertInGraveyard(player1, "View from Above");
    }

    @Test
    @DisplayName("An illegal sole target prevents the spell from returning even with a white permanent")
    void removedTargetPreventsReturn() {
        harness.addToBattlefield(player1, new VedalkenOutlander());
        UUID target = castOnOutlander();
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, target);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Zombie Outlander");
        harness.assertNotInHand(player1, "View from Above");
        harness.assertInGraveyard(player1, "View from Above");
    }

    @Test
    @DisplayName("Losing the white permanent before resolution prevents return")
    void whitePermanentLeavesBeforeResolution() {
        harness.addToBattlefield(player1, new VedalkenOutlander());
        UUID whitePermanent = harness.getPermanentId(player1, "Vedalken Outlander");
        castOnOutlander();
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, whitePermanent);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Zombie Outlander").getGrantedKeywords()).contains(Keyword.FLYING);
        harness.assertNotInHand(player1, "View from Above");
        harness.assertInGraveyard(player1, "View from Above");
    }

    @Test
    @DisplayName("A white permanent entering before resolution enables return")
    void whitePermanentEntersBeforeResolution() {
        castOnOutlander();
        harness.addToBattlefield(player1, new VedalkenOutlander());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Zombie Outlander").getGrantedKeywords()).contains(Keyword.FLYING);
        harness.assertInHand(player1, "View from Above");
        harness.assertNotInGraveyard(player1, "View from Above");
    }

    @Test
    @DisplayName("A white noncreature permanent satisfies the return condition")
    void whiteEnchantmentReturnsSpell() {
        harness.addToBattlefield(player1, new MarkOfAsylum());
        castOnOutlander();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Zombie Outlander").getGrantedKeywords()).contains(Keyword.FLYING);
        harness.assertInHand(player1, "View from Above");
        harness.assertNotInGraveyard(player1, "View from Above");
    }
}
