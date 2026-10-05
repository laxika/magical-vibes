package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.s.Soar;
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

@CardUsed({MistDragon.class, Soar.class})
class MistDragonTest extends BaseCardTest {

    @Test
    @DisplayName("The first {0} ability gives flying")
    void gainsFlying() {
        Permanent dragon = addCreatureReady(player1, new MistDragon());
        assertThat(gqs.hasKeyword(gd, dragon, Keyword.FLYING)).isFalse();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, dragon, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("The flying grant lasts indefinitely — it survives end of turn")
    void flyingLastsIndefinitely() {
        Permanent dragon = addCreatureReady(player1, new MistDragon());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, dragon, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("The second {0} ability takes flying away again")
    void losesFlying() {
        Permanent dragon = addCreatureReady(player1, new MistDragon());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, dragon, Keyword.FLYING)).isTrue();

        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, dragon, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Gaining flying after losing it wins again — the latest activation applies")
    void latestActivationWins() {
        Permanent dragon = addCreatureReady(player1, new MistDragon());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, dragon, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("The {3}{U}{U} ability phases Mist Dragon out")
    void phasesOut() {
        Permanent dragon = addCreatureReady(player1, new MistDragon());
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(dragon);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(dragon);
    }

    @Test
    @DisplayName("Mist Dragon phases back in during its controller's next untap step, keeping flying")
    void phasesBackInWithFlying() {
        Permanent dragon = addCreatureReady(player1, new MistDragon());
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(dragon);

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(dragon);

        harness.passUntilWithNoAttackers(player1, TurnStep.UPKEEP);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(dragon);
        assertThat(gqs.hasKeyword(gd, dragon, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("A later Soar grants flying after Mist Dragon loses it")
    void laterAuraGrantsFlyingAfterLoss() {
        Permanent dragon = addCreatureReady(player1, new MistDragon());
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, dragon, Keyword.FLYING)).isFalse();

        harness.setHand(player1, List.of(new Soar()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.clearPriorityPassed();
        harness.castEnchantment(player1, 0, dragon.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, dragon, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Mist Dragon can remove flying granted by an earlier Soar")
    void laterLossRemovesAuraFlying() {
        Permanent dragon = addCreatureReady(player1, new MistDragon());
        harness.setHand(player1, List.of(new Soar()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castEnchantment(player1, 0, dragon.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, dragon, Keyword.FLYING)).isTrue();

        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, dragon, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("An ability resolving while Mist Dragon is phased out cannot grant flying")
    void pendingFlyingGrantDoesNotAffectPhasedOutDragon() {
        Permanent dragon = addCreatureReady(player1, new MistDragon());
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(dragon);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();

        harness.passUntilWithNoAttackers(player1, TurnStep.UPKEEP);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(dragon);
        assertThat(gqs.hasKeyword(gd, dragon, Keyword.FLYING)).isFalse();
    }

}
