package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AgathasChampion.class, DarksteelRelic.class, GrizzlyBears.class})
class AgathasChampionTest extends BaseCardTest {

    @Test
    void withoutBargainDoesNotFight() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new AgathasChampion()));
        addMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    void withBargainFightsTargetCreature() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new DarksteelRelic());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new AgathasChampion()));
        addMana();

        harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, target.getId(), null,
                List.of(), List.of(), false, sacrifice.getId(), null, null, null, null, true);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        harness.assertInGraveyard(player1, "Darksteel Relic");
    }

    @Test
    void bargainCanFightNoCreature() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new DarksteelRelic());
        harness.setHand(player1, List.of(new AgathasChampion()));
        addMana();

        harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, null, null,
                List.of(), List.of(), false, sacrifice.getId(), null, null, null, null, true);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Agatha's Champion");
        harness.assertInGraveyard(player1, "Darksteel Relic");
    }

    @Test
    void bargainCannotTargetOwnCreature() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new DarksteelRelic());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new AgathasChampion()));
        addMana();

        assertThatThrownBy(() -> harness.getGameService().playCard(harness.getGameData(), player1, 0, 0,
                target.getId(), null, List.of(), List.of(), false, sacrifice.getId(), null, null, null, null, true))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void bargainCannotSacrificeCreature() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new AgathasChampion()));
        addMana();

        assertThatThrownBy(() -> harness.getGameService().playCard(harness.getGameData(), player1, 0, 0,
                null, null, List.of(), List.of(), false, sacrifice.getId(), null, null, null, null, true))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
