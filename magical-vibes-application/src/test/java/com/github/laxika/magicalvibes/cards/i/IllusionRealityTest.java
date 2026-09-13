package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.m.MaskOfIntolerance;
import com.github.laxika.magicalvibes.cards.u.UrborgElf;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IllusionReality.class, MaskOfIntolerance.class, UrborgElf.class})
class IllusionRealityTest extends BaseCardTest {

    @Test
    void illusionChangesPermanentToChosenColorUntilEndOfTurn() {
        Permanent elf = harness.addToBattlefieldAndReturn(player2, new UrborgElf());
        harness.setHand(player1, List.of(new IllusionReality()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, 0, elf.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, "RED");

        assertThat(gqs.getEffectiveColors(gd, elf)).containsExactly(CardColor.RED);

        gd.expireEndOfTurnFloatingEffects();
        elf.resetModifiers();
        assertThat(gqs.getEffectiveColors(gd, elf)).containsExactly(CardColor.GREEN);
    }

    @Test
    void illusionChangesTargetSpellToChosenColor() {
        harness.setHand(player1, List.of(new IllusionReality(), new UrborgElf()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 1);
        UUID elfSpellId = gd.stack.getFirst().getCard().getId();
        harness.castInstant(player1, 0, 0, elfSpellId);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, "BLUE");
        harness.passBothPriorities();

        Permanent elf = findPermanent(player1, "Urborg Elf");
        assertThat(gqs.getEffectiveColors(gd, elf)).containsExactly(CardColor.BLUE);
    }

    @Test
    void realityDestroysTargetArtifact() {
        harness.addToBattlefield(player2, new MaskOfIntolerance());
        harness.setHand(player1, List.of(new IllusionReality()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID targetId = harness.getPermanentId(player2, "Mask of Intolerance");
        harness.castInstant(player1, 0, 1, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Mask of Intolerance");
        harness.assertInGraveyard(player2, "Mask of Intolerance");
    }

    @Test
    void realityCannotTargetCreature() {
        harness.addToBattlefield(player2, new UrborgElf());
        harness.setHand(player1, List.of(new IllusionReality()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(
                player1, 0, 1, harness.getPermanentId(player2, "Urborg Elf")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void illusionChangesNoncreaturePermanentToChosenColor() {
        Permanent mask = harness.addToBattlefieldAndReturn(player2, new MaskOfIntolerance());
        harness.setHand(player1, List.of(new IllusionReality()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, 0, mask.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, "BLUE");

        assertThat(gqs.getEffectiveColors(gd, mask)).containsExactly(CardColor.BLUE);
    }

    @Test
    void realityCannotTargetArtifactSpell() {
        harness.setHand(player1, List.of(new IllusionReality(), new MaskOfIntolerance()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 1);
        UUID artifactSpellId = gd.stack.getFirst().getCard().getId();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, artifactSpellId))
                .isInstanceOf(IllegalStateException.class);
    }
}
