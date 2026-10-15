package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BearCub;
import com.github.laxika.magicalvibes.cards.s.SavannahLions;
import com.github.laxika.magicalvibes.cards.r.RiteOfReplication;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArahboTheFirstFang.class, SavannahLions.class, BearCub.class, RiteOfReplication.class})
class ArahboTheFirstFangTest extends BaseCardTest {

    @Test
    @DisplayName("Other Cats you control get +1/+1")
    void buffsOtherCatsYouControl() {
        harness.addToBattlefield(player1, new ArahboTheFirstFang());
        harness.addToBattlefield(player1, new SavannahLions());
        harness.addToBattlefield(player1, new BearCub());

        Permanent arahbo = findPermanent(player1, "Arahbo, the First Fang");
        Permanent cat = findPermanent(player1, "Savannah Lions");
        Permanent bears = findPermanent(player1, "Bear Cub");

        assertThat(gqs.getEffectivePower(gd, arahbo)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, arahbo)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, cat)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, cat)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Arahbo entering creates a Cat token")
    void ownEntryCreatesCatToken() {
        harness.setHand(player1, List.of(new ArahboTheFirstFang()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Cat")).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Another nontoken Cat entering creates a Cat token")
    void anotherNontokenCatEntryCreatesCatToken() {
        harness.addToBattlefield(player1, new ArahboTheFirstFang());
        harness.setHand(player1, List.of(new SavannahLions()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Cat")).isEqualTo(1);
    }

    @Test
    @DisplayName("Non-Cats and Cat tokens do not trigger the ability")
    void nonCatsAndCatTokensDoNotTrigger() {
        harness.setHand(player1, List.of(new ArahboTheFirstFang()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Cat")).isEqualTo(1);
        assertThat(gd.stack).isEmpty();

        harness.setHand(player1, List.of(new BearCub()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Cat")).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }
    @Test
    @DisplayName("The generated Cat receives Arahbo's boost")
    void generatedCatReceivesBoost() {
        harness.enterBattlefieldAndReturn(player1, new ArahboTheFirstFang());
        resolveAllTriggers();

        Permanent token = findPermanent(player1, "Cat");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent's Cat neither triggers Arahbo nor receives its boost")
    void opponentCatDoesNotTriggerOrReceiveBoost() {
        harness.addToBattlefield(player1, new ArahboTheFirstFang());
        Permanent cat = harness.enterBattlefieldAndReturn(player2, new SavannahLions());

        assertThat(gd.stack).isEmpty();
        assertThat(countPermanents(player1, "Cat")).isZero();
        assertThat(gqs.getEffectivePower(gd, cat)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, cat)).isEqualTo(1);
    }

    @Test
    @DisplayName("A token copy of Arahbo triggers on its own entry")
    void tokenCopyTriggersOnOwnEntry() {
        harness.addToBattlefield(player2, new ArahboTheFirstFang());
        Permanent original = findPermanent(player2, "Arahbo, the First Fang");
        harness.setHand(player1, List.of(new RiteOfReplication()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, original.getId());
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Arahbo, the First Fang")).isEqualTo(1);
        assertThat(findPermanent(player1, "Arahbo, the First Fang").getCard().isToken()).isTrue();
        assertThat(countPermanents(player1, "Cat")).isEqualTo(1);
        assertThat(countPermanents(player2, "Cat")).isZero();
        assertThat(gd.stack).isEmpty();
    }
}

