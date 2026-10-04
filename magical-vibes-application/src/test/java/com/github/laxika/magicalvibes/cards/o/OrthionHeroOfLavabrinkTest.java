package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OrthionHeroOfLavabrink.class, GrizzlyBears.class})
class OrthionHeroOfLavabrinkTest extends BaseCardTest {

    @Test
    @DisplayName("The first ability creates one hasty copy scheduled for sacrifice")
    void firstAbilityCreatesOneHastyCopy() {
        addOrthionReady(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(2);
        Permanent token = findPermanents(player1, "Grizzly Bears").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().getKeywords()).contains(Keyword.HASTE);
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .contains(new DelayedPermanentAction(token.getId(), DelayedPermanentActionKind.SACRIFICE_AT_END_STEP));
    }

    @Test
    @DisplayName("The second ability creates five hasty copies")
    void secondAbilityCreatesFiveHastyCopies() {
        addOrthionReady(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 9);

        harness.activateAbility(player1, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(6);
        assertThat(findPermanents(player1, "Grizzly Bears")).filteredOn(permanent ->
                permanent.getCard().isToken()).hasSize(5)
                .allMatch(permanent -> permanent.getCard().getKeywords().contains(Keyword.HASTE));
    }

    @Test
    @DisplayName("The abilities require another creature you control")
    void abilitiesRequireAnotherCreatureYouControl() {
        Permanent orthion = addOrthionReady(player1);
        harness.addMana(player1, ManaColor.RED, 20);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, orthion.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another creature you control");

        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another creature you control");
    }

    private Permanent addOrthionReady(Player player) {
        return addCreatureReady(player, new OrthionHeroOfLavabrink());
    }
}
