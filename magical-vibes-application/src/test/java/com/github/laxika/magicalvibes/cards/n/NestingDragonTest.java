package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NestingDragon.class, Forest.class, DoomBlade.class})
class NestingDragonTest extends BaseCardTest {

    @Test
    @DisplayName("Landfall creates a 0/2 red Dragon Egg token with defender")
    void landfallCreatesDragonEgg() {
        harness.addToBattlefield(player1, new NestingDragon());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        Permanent egg = findPermanent(player1, "Dragon Egg");
        assertThat(egg.getCard().getPower()).isEqualTo(0);
        assertThat(egg.getCard().getToughness()).isEqualTo(2);
        assertThat(egg.getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(egg.getCard().getSubtypes()).contains(CardSubtype.EGG);
        assertThat(egg.getCard().getKeywords()).contains(Keyword.DEFENDER);
        assertThat(egg.getCard().isToken()).isTrue();
    }

    @Test
    @DisplayName("When a Dragon Egg dies, it creates a 2/2 red flying Dragon token")
    void dragonEggDeathCreatesDragon() {
        Permanent egg = createDragonEgg();

        destroyEgg(egg);

        Permanent dragon = findPermanent(player1, "Dragon");
        assertThat(dragon.getCard().getPower()).isEqualTo(2);
        assertThat(dragon.getCard().getToughness()).isEqualTo(2);
        assertThat(dragon.getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(dragon.getCard().getSubtypes()).contains(CardSubtype.DRAGON);
        assertThat(dragon.getCard().getKeywords()).contains(Keyword.FLYING);
        assertThat(dragon.getCard().isToken()).isTrue();
    }

    @Test
    @DisplayName("The Dragon token's firebreathing ability lasts until end of turn")
    void dragonTokenHasFirebreathing() {
        Permanent egg = createDragonEgg();
        destroyEgg(egg);

        GameData gd = harness.getGameData();
        int dragonIndex = gd.playerBattlefields.get(player1.getId()).indexOf(findPermanent(player1, "Dragon"));

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, dragonIndex, null, null);
        harness.passBothPriorities();

        Permanent dragon = findPermanent(player1, "Dragon");
        assertThat(dragon.getEffectivePower()).isEqualTo(3);
        assertThat(dragon.getEffectiveToughness()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(dragon.getEffectivePower()).isEqualTo(2);
    }

    private Permanent createDragonEgg() {
        harness.addToBattlefield(player1, new NestingDragon());
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();
        return findPermanent(player1, "Dragon Egg");
    }

    private void destroyEgg(Permanent egg) {
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castInstant(player1, 0, egg.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
