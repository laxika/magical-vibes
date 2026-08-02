package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BroodKeeperTest extends BaseCardTest {

    @Test
    @DisplayName("An Aura becoming attached to Brood Keeper creates a 2/2 red flying Dragon token")
    void auraAttachCreatesDragonToken() {
        Permanent keeper = addCreatureReady(player1, new BroodKeeper());

        enchantWithHolyStrength(player1, keeper);

        List<Permanent> tokens = findPermanents(player1, "Dragon");
        assertThat(tokens).hasSize(1);
        Permanent token = tokens.getFirst();
        assertThat(token.getCard().getPower()).isEqualTo(2);
        assertThat(token.getCard().getToughness()).isEqualTo(2);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.DRAGON);
        assertThat(token.getCard().getKeywords()).contains(Keyword.FLYING);
        assertThat(token.getCard().isToken()).isTrue();
    }

    @Test
    @DisplayName("An opponent's Aura attaching to Brood Keeper still creates a token for its controller")
    void opponentAuraTriggersForKeeperController() {
        Permanent keeper = addCreatureReady(player1, new BroodKeeper());

        enchantWithHolyStrength(player2, keeper);

        assertThat(findPermanents(player1, "Dragon")).hasSize(1);
        assertThat(findPermanents(player2, "Dragon")).isEmpty();
    }

    @Test
    @DisplayName("An Aura attaching to another creature does not trigger Brood Keeper")
    void auraOnOtherCreatureDoesNotTrigger() {
        addCreatureReady(player1, new BroodKeeper());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        enchantWithHolyStrength(player1, bears);

        assertThat(findPermanents(player1, "Dragon")).isEmpty();
    }

    @Test
    @DisplayName("The Dragon token's {R} ability gives it +1/+0 until end of turn")
    void dragonTokenHasFirebreathing() {
        Permanent keeper = addCreatureReady(player1, new BroodKeeper());

        enchantWithHolyStrength(player1, keeper);

        int tokenIndex = gd.playerBattlefields.get(player1.getId())
                .indexOf(findPermanents(player1, "Dragon").getFirst());

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, tokenIndex, null, null);
        harness.passBothPriorities();

        Permanent token = findPermanents(player1, "Dragon").getFirst();
        assertThat(token.getEffectivePower()).isEqualTo(3);
        assertThat(token.getEffectiveToughness()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(token.getEffectivePower()).isEqualTo(2);
    }

    private void enchantWithHolyStrength(Player controller, Permanent target) {
        harness.forceActivePlayer(controller);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(controller, List.of(new HolyStrength()));
        harness.addMana(controller, ManaColor.WHITE, 1);

        harness.castEnchantment(controller, 0, target.getId());
        harness.passBothPriorities(); // resolve the Aura (attach)
        harness.passBothPriorities(); // resolve Brood Keeper's trigger, if any
    }
}
