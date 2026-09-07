package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SorinVengefulBloodlord.class, ChandraNalaar.class, GrizzlyBears.class, HillGiant.class})
class SorinVengefulBloodlordTest extends BaseCardTest {

    @Test
    @DisplayName("During your turn, creatures and planeswalkers you control have lifelink")
    void grantsLifelinkToControlledCreaturesAndPlaneswalkersDuringYourTurn() {
        Permanent sorin = addReadySorin(player1, 4);
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent chandra = harness.addToBattlefieldAndReturn(player1, new ChandraNalaar());
        chandra.setCounterCount(CounterType.LOYALTY, 3);
        Permanent opposingBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, sorin, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, chandra, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingBear, Keyword.LIFELINK)).isFalse();

        harness.forceActivePlayer(player2);

        assertThat(gqs.hasKeyword(gd, sorin, Keyword.LIFELINK)).isFalse();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.LIFELINK)).isFalse();
        assertThat(gqs.hasKeyword(gd, chandra, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("+2 deals 1 damage to a target player")
    void plusTwoDealsDamageToTargetPlayer() {
        Permanent sorin = addReadySorin(player1, 4);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(sorin.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("-X returns a matching creature as a Vampire")
    void minusXReturnsMatchingCreatureAsVampire() {
        Permanent sorin = addReadySorin(player1, 5);
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));

        harness.activateAbility(player1, 0, 1, 2, bears.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Grizzly Bears");
        assertThat(sorin.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(gqs.effectiveCreatureSubtypes(gd, returned)).contains(CardSubtype.BEAR, CardSubtype.VAMPIRE);
    }

    @Test
    @DisplayName("-X rejects a creature card whose mana value differs from X")
    void minusXRejectsWrongManaValue() {
        addReadySorin(player1, 5);
        Card giant = new HillGiant();
        harness.setGraveyard(player1, List.of(giant));

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, 1, 2, giant.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadySorin(Player player, int loyalty) {
        Permanent sorin = new Permanent(new SorinVengefulBloodlord());
        sorin.setCounterCount(CounterType.LOYALTY, loyalty);
        sorin.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(sorin);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return sorin;
    }
}
