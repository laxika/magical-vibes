package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StormbeaconBlade.class, GrizzlyBears.class})
class StormbeaconBladeTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +3/+0")
    void equippedCreatureGetsPowerBoost() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent blade = addCreatureReady(player1, new StormbeaconBlade());
        blade.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Draws when three creatures you control attack")
    void drawsWhenThreeControlledCreaturesAttack() {
        setUpBattlefieldAndLibrary();
        Permanent blade = gd.playerBattlefields.get(player1.getId()).get(0);
        Permanent equippedCreature = gd.playerBattlefields.get(player1.getId()).get(1);
        blade.setAttachedTo(equippedCreature.getId());

        declareAttackers(player1, List.of(1, 2, 3));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not draw when fewer than three creatures you control attack")
    void doesNotDrawWhenFewerThanThreeControlledCreaturesAttack() {
        setUpBattlefieldAndLibrary();
        Permanent blade = gd.playerBattlefields.get(player1.getId()).get(0);
        Permanent equippedCreature = gd.playerBattlefields.get(player1.getId()).get(1);
        blade.setAttachedTo(equippedCreature.getId());

        declareAttackers(player1, List.of(1, 2));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not trigger when the Blade is unattached")
    void doesNotTriggerWhenUnattached() {
        setUpBattlefieldAndLibrary();

        declareAttackers(player1, List.of(1, 2, 3));

        assertThat(gd.stack)
                .noneMatch(se -> se.getCard().getName().equals("Stormbeacon Blade"));
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private void setUpBattlefieldAndLibrary() {
        addCreatureReady(player1, new StormbeaconBlade());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Card()));
    }
}
