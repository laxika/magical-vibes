package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.c.CopperMyr;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MyrSire;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UrtetRemnantOfMemnarch.class, MyrSire.class, CopperMyr.class, GrizzlyBears.class})
class UrtetRemnantOfMemnarchTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a Myr spell creates a Myr token")
    void myrSpellCreatesToken() {
        harness.addToBattlefield(player1, new UrtetRemnantOfMemnarch());
        harness.setHand(player1, List.of(new MyrSire()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Myr")
                        && permanent.getCard().isToken()
                        && permanent.getCard().hasType(CardType.ARTIFACT)
                        && permanent.getCard().hasType(CardType.CREATURE));
    }

    @Test
    @DisplayName("Casting a non-Myr spell does not create a token")
    void nonMyrSpellDoesNotCreateToken() {
        harness.addToBattlefield(player1, new UrtetRemnantOfMemnarch());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Myr"));
    }

    @Test
    @DisplayName("Beginning of combat untaps each Myr you control")
    void beginningOfCombatUntapsOwnMyr() {
        Permanent urtet = addReady(player1, new UrtetRemnantOfMemnarch());
        Permanent ownMyr = addReady(player1, new CopperMyr());
        Permanent ownNonMyr = addReady(player1, new GrizzlyBears());
        Permanent opponentMyr = addReady(player2, new CopperMyr());

        urtet.tap();
        ownMyr.tap();
        ownNonMyr.tap();
        opponentMyr.tap();

        advanceToCombat(player1);
        harness.passBothPriorities();

        assertThat(urtet.isTapped()).isFalse();
        assertThat(ownMyr.isTapped()).isFalse();
        assertThat(ownNonMyr.isTapped()).isTrue();
        assertThat(opponentMyr.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Activated ability puts three counters on each own Myr")
    void activatedAbilityPutsCountersOnOwnMyr() {
        Permanent urtet = addReady(player1, new UrtetRemnantOfMemnarch());
        Permanent ownMyr = addReady(player1, new CopperMyr());
        Permanent ownNonMyr = addReady(player1, new GrizzlyBears());
        Permanent opponentMyr = addReady(player2, new CopperMyr());

        addFiveColorMana(player1);
        int urtetIndex = gd.playerBattlefields.get(player1.getId()).indexOf(urtet);
        harness.activateAbility(player1, urtetIndex, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, urtet)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, urtet)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, ownMyr)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ownMyr)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, ownNonMyr)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownNonMyr)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentMyr)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponentMyr)).isEqualTo(1);
        assertThat(urtet.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Activated ability can be used only during your turn")
    void activatedAbilityRequiresYourTurn() {
        addReady(player1, new UrtetRemnantOfMemnarch());
        addFiveColorMana(player1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("during your turn");
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }

    private void addFiveColorMana(Player player) {
        harness.addMana(player, ManaColor.WHITE, 1);
        harness.addMana(player, ManaColor.BLUE, 1);
        harness.addMana(player, ManaColor.BLACK, 1);
        harness.addMana(player, ManaColor.RED, 1);
        harness.addMana(player, ManaColor.GREEN, 1);
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
