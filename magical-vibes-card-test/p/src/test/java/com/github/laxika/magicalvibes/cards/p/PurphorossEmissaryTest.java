package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BronzeSable;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PurphorossEmissary.class, BronzeSable.class})
class PurphorossEmissaryTest extends BaseCardTest {

    @Test
    @DisplayName("Bestow boosts the enchanted creature and grants it menace")
    void castsForBestow() {
        Permanent host = harness.addToBattlefieldAndReturn(player1, new BronzeSable());
        harness.setHand(player1, List.of(new PurphorossEmissary()));
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castWithAlternateCost(player1, 0, host.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, host)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, host, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("A bestowed Purphoros's Emissary becomes a creature when its host leaves")
    void becomesCreatureWhenHostLeaves() {
        Permanent host = harness.addToBattlefieldAndReturn(player1, new BronzeSable());
        harness.setHand(player1, List.of(new PurphorossEmissary()));
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castWithAlternateCost(player1, 0, host.getId());
        harness.passBothPriorities();
        Permanent emissary = gqs.findPermanentById(gd, harness.getPermanentId(player1, "Purphoros's Emissary"));

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, host));
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(emissary);
        assertThat(gqs.isCreature(gd, emissary)).isTrue();
        assertThat(emissary.isAttached()).isFalse();
    }

    @Test
    @DisplayName("Normal casting does not require an enchant target")
    void castsNormallyWithoutTarget() {
        harness.setHand(player1, List.of(new PurphorossEmissary()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent emissary = gqs.findPermanentById(gd,
                harness.getPermanentId(player1, "Purphoros's Emissary"));
        assertThat(gqs.isCreature(gd, emissary)).isTrue();
        assertThat(emissary.isAttached()).isFalse();
        harness.assertNotInGraveyard(player1, "Purphoros's Emissary");
    }

    @Test
    @DisplayName("Bestow resolves as a creature when its target leaves before resolution")
    void resolvesAsCreatureWhenTargetLeaves() {
        Permanent host = harness.addToBattlefieldAndReturn(player2, new BronzeSable());
        harness.setHand(player1, List.of(new PurphorossEmissary()));
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castWithAlternateCost(player1, 0, host.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, host));
        harness.runStateBasedActions();
        harness.passBothPriorities();

        Permanent emissary = gqs.findPermanentById(gd,
                harness.getPermanentId(player1, "Purphoros's Emissary"));
        assertThat(gqs.isCreature(gd, emissary)).isTrue();
        assertThat(emissary.isAttached()).isFalse();
        harness.assertNotInGraveyard(player1, "Purphoros's Emissary");
    }

    @Test
    @DisplayName("Bestow can enchant an opposing creature and its bonuses end when the Aura leaves")
    void enchantsOpposingCreatureAndBonusesEndWhenAuraLeaves() {
        Permanent host = harness.addToBattlefieldAndReturn(player2, new BronzeSable());
        harness.setHand(player1, List.of(new PurphorossEmissary()));
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castWithAlternateCost(player1, 0, host.getId());
        harness.passBothPriorities();

        Permanent emissary = gqs.findPermanentById(gd,
                harness.getPermanentId(player1, "Purphoros's Emissary"));
        assertThat(gqs.isCreature(gd, emissary)).isFalse();
        assertThat(emissary.getAttachedTo()).isEqualTo(host.getId());
        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, host)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, host, Keyword.MENACE)).isTrue();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, emissary));
        harness.runStateBasedActions();

        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, host)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, host, Keyword.MENACE)).isFalse();
        harness.assertInGraveyard(player1, "Purphoros's Emissary");
    }
}
