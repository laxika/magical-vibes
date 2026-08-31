package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CrownOfAwe.class, GrizzlyBears.class, RagingGoblin.class, SuntailHawk.class})
class CrownOfAweTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature has protection from black and red")
    void enchantedCreatureHasProtectionFromBlackAndRed() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        attachCrown(bears);

        assertThat(gqs.hasProtectionFrom(gd, bears, CardColor.BLACK)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, bears, CardColor.RED)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, bears, CardColor.BLUE)).isFalse();
    }

    @Test
    @DisplayName("Sacrificing the Aura protects the enchanted creature and creatures sharing its type")
    void sacrificeProtectsEnchantedAndSharingCreatures() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent otherBears = addCreatureReady(player2, new GrizzlyBears());
        Permanent goblin = addCreatureReady(player1, new RagingGoblin());
        Permanent hawk = addCreatureReady(player2, new SuntailHawk());
        Permanent crown = attachCrown(bears);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(crown), null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasProtectionFrom(gd, bears, CardColor.BLACK)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, bears, CardColor.RED)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, otherBears, CardColor.BLACK)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, otherBears, CardColor.RED)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, goblin, CardColor.BLACK)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, hawk, CardColor.RED)).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(crown);
    }

    @Test
    @DisplayName("Sacrifice protection wears off at end of turn")
    void sacrificeProtectionWearsOffAtEndOfTurn() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent otherBears = addCreatureReady(player2, new GrizzlyBears());
        attachCrown(bears);

        Permanent crown = findPermanent(player1, "Crown of Awe");
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(crown), null, null);
        harness.passBothPriorities();
        assertThat(gqs.hasProtectionFrom(gd, otherBears, CardColor.BLACK)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasProtectionFrom(gd, bears, CardColor.BLACK)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, otherBears, CardColor.BLACK)).isFalse();
    }

    private Permanent attachCrown(Permanent host) {
        Permanent crown = new Permanent(new CrownOfAwe());
        crown.setAttachedTo(host.getId());
        gd.playerBattlefields.get(player1.getId()).add(crown);
        return crown;
    }
}
