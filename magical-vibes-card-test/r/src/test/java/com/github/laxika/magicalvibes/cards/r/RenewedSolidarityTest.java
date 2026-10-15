package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

@CardUsed({RenewedSolidarity.class, GrizzlyBears.class, HillGiant.class})
class RenewedSolidarityTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures of the chosen type get +1/+0")
    void boostsCreaturesOfChosenType() {
        castRenewedSolidarity(CardSubtype.BEAR);

        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());

        assertThat(gqs.computeStaticBonus(gd, bear).power()).isEqualTo(1);
        assertThat(gqs.computeStaticBonus(gd, bear).toughness()).isEqualTo(0);
        assertThat(gqs.computeStaticBonus(gd, giant).power()).isEqualTo(0);
    }

    @Test
    @DisplayName("At your end step, copies matching tokens that entered this turn")
    void copiesMatchingTokensThatEnteredThisTurn() {
        castRenewedSolidarity(CardSubtype.GOBLIN);

        addExistingToken(player1, "Older Goblin", CardSubtype.GOBLIN);
        addToken(player1, "Goblin", CardSubtype.GOBLIN);
        addToken(player1, "Elf", CardSubtype.ELF);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(tokenPermanents(player1, "Older Goblin")).hasSize(1);
        assertThat(tokenPermanents(player1, "Goblin")).hasSize(2);
        assertThat(tokenPermanents(player1, "Elf")).hasSize(1);
    }

    private void castRenewedSolidarity(CardSubtype chosenSubtype) {
        harness.setHand(player1, List.of(new RenewedSolidarity()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, chosenSubtype.name());
    }

    private Permanent addExistingToken(Player player, String name, CardSubtype subtype) {
        return harness.addToBattlefieldAndReturn(player, createToken(name, subtype));
    }

    private Permanent addToken(Player player, String name, CardSubtype subtype) {
        return harness.enterBattlefieldAndReturn(player, createToken(name, subtype));
    }

    private Card createToken(String name, CardSubtype subtype) {
        Card token = new Card();
        token.setName(name);
        token.setType(CardType.CREATURE);
        token.setManaCost("");
        token.setColor(CardColor.GREEN);
        token.setPower(1);
        token.setToughness(1);
        token.setSubtypes(List.of(subtype));
        token.setToken(true);
        return token;
    }

    private List<Permanent> tokenPermanents(Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getName().equals(name))
                .toList();
    }
}
