package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChoMannosBlessingTest extends BaseCardTest {

    @Test
    void enchantedCreatureHasProtectionFromChosenColorOnly() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent blessing = new Permanent(new ChoMannosBlessing());
        blessing.setAttachedTo(bears.getId());
        blessing.setChosenColor(CardColor.BLACK);
        gd.playerBattlefields.get(player1.getId()).add(blessing);

        assertThat(gqs.hasProtectionFrom(gd, bears, CardColor.BLACK)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, bears, CardColor.RED)).isFalse();
    }

    @Test
    void choosingColorDoesNotRemoveTheAuraItself() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent blessing = new Permanent(new ChoMannosBlessing());
        blessing.setAttachedTo(bears.getId());
        blessing.setChosenColor(CardColor.WHITE);
        gd.playerBattlefields.get(player1.getId()).add(blessing);

        boolean changed = GameTestEngineContext.get().getBean(PermanentRemovalService.class)
                .enforceAttachmentLegality(gd);

        assertThat(changed).isFalse();
        harness.assertOnBattlefield(player1, "Cho-Manno's Blessing");
    }

    @Test
    void castingItChoosesColorAndGrantsProtection() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ChoMannosBlessing()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLUE");

        assertThat(gqs.hasProtectionFrom(gd, bears, CardColor.BLUE)).isTrue();
    }

    @Test
    void cannotEnchantNonCreaturePermanent() {
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new ChoMannosBlessing()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        Permanent artifact = findPermanent(player1, "Fountain of Youth");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
