package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(TheUnbeatableSquirrelGirl.class)
class TheUnbeatableSquirrelGirlTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Squirrel token when it enters")
    void enteringCreatesSquirrelToken() {
        castSquirrelGirl();

        List<Permanent> squirrels = squirrelTokens(player1);
        assertThat(squirrels).hasSize(1);
        assertThat(squirrels.getFirst().getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(squirrels.getFirst().getCard().getSubtypes()).contains(CardSubtype.SQUIRREL);
        assertThat(squirrels.getFirst().getEffectivePower()).isEqualTo(1);
        assertThat(squirrels.getFirst().getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Creates a Squirrel token when it attacks")
    void attackingCreatesSquirrelToken() {
        addCreatureReady(player1, new TheUnbeatableSquirrelGirl());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(squirrelTokens(player1)).hasSize(1);
    }

    @Test
    @DisplayName("Creates one token for each Squirrel controlled when activated")
    void activatedAbilityCountsControlledSquirrels() {
        castSquirrelGirl();
        Permanent squirrelGirl = findPermanent(player1, "The Unbeatable Squirrel Girl");
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(squirrelGirl), null, null);
        harness.passBothPriorities();

        assertThat(squirrelTokens(player1)).hasSize(3);
    }

    private void castSquirrelGirl() {
        harness.setHand(player1, List.of(new TheUnbeatableSquirrelGirl()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private List<Permanent> squirrelTokens(com.github.laxika.magicalvibes.model.Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.SQUIRREL))
                .toList();
    }
}
