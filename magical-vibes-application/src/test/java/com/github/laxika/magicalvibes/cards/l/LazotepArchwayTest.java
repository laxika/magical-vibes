package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(LazotepArchway.class)
class LazotepArchwayTest extends BaseCardTest {

    @Test
    void entersTheBattlefieldTapped() {
        harness.setHand(player1, List.of(new LazotepArchway()));

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Lazotep Archway").isTapped()).isTrue();
    }

    @Test
    void addsOneManaOfTheChosenColor() {
        harness.addToBattlefield(player1, new LazotepArchway());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "WHITE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
    }

    @Test
    void eternalizeCreatesTappedCreatureLandZombieToken() {
        harness.forceActivePlayer(player1);
        harness.setGraveyard(player1, List.of(new LazotepArchway()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Lazotep Archway");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Lazotep Archway"));

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Lazotep Archway")
                        && permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.isTapped()).isTrue();
        assertThat(token.getEffectivePower()).isEqualTo(4);
        assertThat(token.getEffectiveToughness()).isEqualTo(4);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.ZOMBIE);
        assertThat(token.getCard().hasType(CardType.LAND)).isTrue();
        assertThat(token.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(token.getCard().getManaCost()).isEmpty();
    }
}
