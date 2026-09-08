package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.c.CloudSprite;
import com.github.laxika.magicalvibes.cards.d.DrudgeSkeletons;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JayaBallardTaskMage.class, DrudgeSkeletons.class, CloudSprite.class, GrizzlyBears.class})
class JayaBallardTaskMageTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a target blue permanent after discarding a card")
    void destroysTargetBluePermanent() {
        addReadyJaya(player1);
        harness.addToBattlefield(player2, new CloudSprite());
        Card discarded = new GrizzlyBears();
        harness.setHand(player1, List.of(discarded));
        harness.addMana(player1, ManaColor.RED, 1);

        Permanent target = findPermanent(player2, "Cloud Sprite");
        harness.activateAbility(player1, 0, null, target.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Cloud Sprite");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
    }

    @Test
    @DisplayName("Cannot target a non-blue permanent")
    void cannotTargetNonBluePermanent() {
        addReadyJaya(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);

        Permanent target = findPermanent(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a blue permanent");
    }

    @Test
    @DisplayName("Deals 3 damage to any target and prevents regeneration of a damaged creature")
    void dealsDamageAndPreventsRegeneration() {
        addReadyJaya(player1);
        Permanent skeletons = new Permanent(new DrudgeSkeletons());
        skeletons.setSummoningSick(false);
        skeletons.setRegenerationShield(1);
        gd.playerBattlefields.get(player2.getId()).add(skeletons);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, skeletons.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Drudge Skeletons");
        assertThat(skeletons.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deals 6 damage to each creature and each player")
    void dealsDamageToEachCreatureAndPlayer() {
        Permanent jaya = addReadyJaya(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(14);
        assertThat(gd.getLife(player2.getId())).isEqualTo(14);
        harness.assertInGraveyard(player1, "Jaya Ballard, Task Mage");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(jaya.isTapped()).isTrue();
    }

    private Permanent addReadyJaya(Player player) {
        Permanent permanent = new Permanent(new JayaBallardTaskMage());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
