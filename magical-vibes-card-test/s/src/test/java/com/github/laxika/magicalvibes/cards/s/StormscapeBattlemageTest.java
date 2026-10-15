package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AuroraGriffin;
import com.github.laxika.magicalvibes.cards.m.MaggotCarrier;
import com.github.laxika.magicalvibes.cards.m.ManaCylix;
import com.github.laxika.magicalvibes.cards.t.Terminate;
import com.github.laxika.magicalvibes.cards.z.RoostOfDrakes;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StormscapeBattlemage.class, AuroraGriffin.class, MaggotCarrier.class, ManaCylix.class,
        Terminate.class, RoostOfDrakes.class})
class StormscapeBattlemageTest extends BaseCardTest {

    @Test
    @DisplayName("Without kicker, neither ability resolves")
    void noKicker() {
        harness.setHand(player1, List.of(new StormscapeBattlemage()));
        addMana(2, ManaColor.BLUE);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Stormscape Battlemage");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("White kicker gains 3 life")
    void whiteKicker() {
        harness.setHand(player1, List.of(new StormscapeBattlemage()));
        addMana(2, ManaColor.BLUE, ManaColor.WHITE);

        harness.castCreatureWithRepeatedCosts(player1, 0, List.of("{W}"));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 23);
    }

    @Test
    @DisplayName("Black kicker destroys a target nonblack creature without regeneration")
    void blackKicker() {
        Permanent target = addTarget(new AuroraGriffin());
        target.setRegenerationShield(1);
        harness.setHand(player1, List.of(new StormscapeBattlemage()));
        addMana(4, ManaColor.BLUE, ManaColor.BLACK);

        harness.castKickedCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Aurora Griffin");
        harness.assertInGraveyard(player2, "Aurora Griffin");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Both kicker costs resolve their independent abilities")
    void bothKickers() {
        Permanent target = addTarget(new AuroraGriffin());
        harness.setHand(player1, List.of(new StormscapeBattlemage()));
        addMana(4, ManaColor.BLUE, ManaColor.BLACK, ManaColor.WHITE);

        castWithAdditionalCosts(List.of("{W}"), target.getId(), true);
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Aurora Griffin");
        harness.assertLife(player1, 23);
    }

    @Test
    @DisplayName("Losing the destruction target does not counter the separate life gain ability")
    void lifeGainResolvesWhenDestructionTargetLeavesBattlefield() {
        Permanent target = addTarget(new AuroraGriffin());
        harness.setHand(player1, List.of(new StormscapeBattlemage(), new Terminate()));
        addMana(4, ManaColor.BLUE, ManaColor.BLACK, ManaColor.WHITE);

        castWithAdditionalCosts(List.of("{W}"), target.getId(), true);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Aurora Griffin");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 23);
        harness.assertOnBattlefield(player1, "Stormscape Battlemage");
    }

    @Test
    @DisplayName("Paying only the white kicker triggers abilities for casting a kicked spell")
    void whiteKickerCountsAsCastingAKickedSpell() {
        harness.addToBattlefield(player1, new RoostOfDrakes());
        harness.setHand(player1, List.of(new StormscapeBattlemage()));
        addMana(2, ManaColor.BLUE, ManaColor.WHITE);

        harness.castCreatureWithRepeatedCosts(player1, 0, List.of("{W}"));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Drake");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertLife(player1, 23);
        harness.assertOnBattlefield(player1, "Stormscape Battlemage");
    }

    @Test
    @DisplayName("Entering without being cast does not trigger either kicker ability")
    void enteringWithoutCastingDoesNotApplyKickerAbilities() {
        addTarget(new AuroraGriffin());

        harness.enterBattlefieldAndReturn(player1, new StormscapeBattlemage());

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertOnBattlefield(player2, "Aurora Griffin");
    }

    @Test
    @DisplayName("Black kicker cannot target a black creature")
    void cannotTargetBlackCreature() {
        Permanent target = addTarget(new MaggotCarrier());
        harness.setHand(player1, List.of(new StormscapeBattlemage()));
        addMana(4, ManaColor.BLUE, ManaColor.BLACK);

        assertThatThrownBy(() -> harness.castKickedCreature(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonblack");
    }

    @Test
    @DisplayName("Black kicker cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent target = addTarget(new ManaCylix());
        harness.setHand(player1, List.of(new StormscapeBattlemage()));
        addMana(4, ManaColor.BLUE, ManaColor.BLACK);

        assertThatThrownBy(() -> harness.castKickedCreature(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonblack creature");
    }

    private Permanent addTarget(Card card) {
        return harness.addToBattlefieldAndReturn(player2, card);
    }

    private void addMana(int colorless, ManaColor... colored) {
        harness.addMana(player1, ManaColor.COLORLESS, colorless);
        for (ManaColor color : colored) {
            harness.addMana(player1, color, 1);
        }
    }

    private void castWithAdditionalCosts(List<String> payments, java.util.UUID targetId, boolean kicked) {
        gs.playCard(gd, player1, 0, 0, targetId, null, List.of(), List.of(), false,
                null, null, null, null, null, kicked, null, null, null, null,
                payments, false);
    }
}
