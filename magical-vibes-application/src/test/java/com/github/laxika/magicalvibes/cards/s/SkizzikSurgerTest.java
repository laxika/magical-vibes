package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SkizzikSurger.class, Forest.class, Mountain.class})
class SkizzikSurgerTest extends BaseCardTest {

    @Test
    void insufficientLandsSacrificeSkizzikSurger() {
        castSkizzikSurger();
        harness.addToBattlefield(player1, new Forest());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Skizzik Surger");
        harness.assertInGraveyard(player1, "Skizzik Surger");
        assertThat(landCount(player1)).isEqualTo(1);
    }

    @Test
    void acceptingEchoSacrificesTwoLandsAndKeepsSkizzikSurger() {
        castSkizzikSurger();
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Mountain());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Skizzik Surger");
        assertThat(landCount(player1)).isZero();
    }

    @Test
    void choosingEchoLandsSacrificesExactlyTwo() {
        castSkizzikSurger();
        harness.addToBattlefield(player1, new Forest());
        Permanent firstMountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent secondMountain = harness.addToBattlefieldAndReturn(player1, new Mountain());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultiplePermanentsChosen(player1, List.of(firstMountain.getId(), secondMountain.getId()));

        harness.assertOnBattlefield(player1, "Skizzik Surger");
        assertThat(landCount(player1)).isEqualTo(1);
    }

    @Test
    void decliningEchoSacrificesSkizzikSurger() {
        castSkizzikSurger();
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Mountain());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Skizzik Surger");
        harness.assertInGraveyard(player1, "Skizzik Surger");
        assertThat(landCount(player1)).isEqualTo(2);
    }

    @Test
    void echoIsOneShot() {
        castSkizzikSurger();
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Mountain());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Skizzik Surger");
    }

    private void castSkizzikSurger() {
        harness.setHand(player1, List.of(new SkizzikSurger()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private long landCount(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().hasType(CardType.LAND))
                .count();
    }
}
