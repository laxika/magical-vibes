package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.k.KnightOfTheStampede;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Bombard.class, GrizzlyBears.class, ColossalDreadmaw.class, KnightOfTheStampede.class})
class BombardTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 4 damage to target creature")
    void dealsFourDamageToTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Bombard()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new Bombard()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Marks exactly four damage on a surviving creature")
    void marksExactlyFourDamage() {
        harness.addToBattlefield(player2, new ColossalDreadmaw());
        harness.setHand(player1, List.of(new Bombard()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Colossal Dreadmaw"));

        harness.assertOnBattlefield(player2, "Colossal Dreadmaw");
        assertThat(findPermanent(player2, "Colossal Dreadmaw").getMarkedDamage()).isEqualTo(4);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Bombard");
    }

    @Test
    @DisplayName("Can target your own creature and deal lethal damage at four toughness")
    void canKillOwnFourToughnessCreature() {
        harness.addToBattlefield(player1, new KnightOfTheStampede());
        harness.setHand(player1, List.of(new Bombard()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Knight of the Stampede"));

        harness.assertNotOnBattlefield(player1, "Knight of the Stampede");
        harness.assertInGraveyard(player1, "Knight of the Stampede");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Does not damage another creature when its target leaves before resolution")
    void targetLeavesBeforeResolution() {
        harness.addToBattlefield(player2, new ColossalDreadmaw());
        harness.addToBattlefield(player2, new KnightOfTheStampede());
        var target = findPermanent(player2, "Colossal Dreadmaw");
        harness.setHand(player1, List.of(new Bombard()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerHands.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Colossal Dreadmaw");
        harness.assertOnBattlefield(player2, "Knight of the Stampede");
        assertThat(findPermanent(player2, "Knight of the Stampede").getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Bombard");
        assertThat(gd.stack).isEmpty();
    }
}
