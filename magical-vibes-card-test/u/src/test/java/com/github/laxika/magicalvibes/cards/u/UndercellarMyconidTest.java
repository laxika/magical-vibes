package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UndercellarMyconid.class, WrathOfGod.class})
class UndercellarMyconidTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield creates a Saproling")
    void enteringCreatesSaproling() {
        castAndResolveMyconid();

        assertThat(findPermanents(player1, "Saproling")).hasSize(1);
    }

    @Test
    @DisplayName("Dying creates a Saproling")
    void dyingCreatesSaproling() {
        harness.addToBattlefield(player1, new UndercellarMyconid());

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castAndResolveSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Saproling")).hasSize(1);
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    @DisplayName("Tapping adds one mana of any color")
    void tapsForAnyColor(ManaColor color) {
        Permanent myconid = addCreatureReady(player1, new UndercellarMyconid());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(myconid.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A summoning-sick Myconid cannot activate its tap ability")
    void summoningSickCannotActivateManaAbility() {
        Permanent myconid = harness.addToBattlefieldAndReturn(player1, new UndercellarMyconid());
        myconid.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(myconid.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Simultaneous deaths create a Saproling for each Myconid's controller")
    void simultaneousDeathsCreateTokensForEachController() {
        harness.addToBattlefield(player1, new UndercellarMyconid());
        harness.addToBattlefield(player2, new UndercellarMyconid());
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveSorcery(player1, 0, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Undercellar Myconid")).isEmpty();
        assertThat(findPermanents(player2, "Undercellar Myconid")).isEmpty();
        assertThat(findPermanents(player1, "Saproling")).hasSize(1);
        assertThat(findPermanents(player2, "Saproling")).hasSize(1);
    }

    private void castAndResolveMyconid() {
        harness.setHand(player1, List.of(new UndercellarMyconid()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
