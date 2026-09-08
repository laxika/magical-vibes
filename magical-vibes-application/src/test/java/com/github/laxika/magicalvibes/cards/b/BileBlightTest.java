package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BileBlightTest extends BaseCardTest {

    @Test
    @DisplayName("Gives the target and all creatures with the same name -3/-3")
    void debuffsTargetAndAllSameNameCreatures() {
        Permanent ownAvatar = addCreature(player1, new AvatarOfMight());
        Permanent target = addCreature(player2, new AvatarOfMight());
        Permanent otherAvatar = addCreature(player2, new AvatarOfMight());
        Permanent elf = addCreature(player2, new LlanowarElves());

        castBileBlight(target.getId());

        assertThat(gqs.getEffectivePower(gd, ownAvatar)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, ownAvatar)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, otherAvatar)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, otherAvatar)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, elf)).isEqualTo(1);
    }

    @Test
    @DisplayName("A same-name hexproof creature is affected without being targeted")
    void affectsSameNameHexproofCreature() {
        Permanent target = addCreature(player2, new AvatarOfMight());
        Permanent hexproof = addCreature(player2, new AvatarOfMight());
        TestCards.mutableCard(hexproof).setKeywords(EnumSet.of(Keyword.HEXPROOF));

        castBileBlight(target.getId());

        assertThat(gqs.getEffectiveToughness(gd, hexproof)).isEqualTo(5);
    }

    @Test
    @DisplayName("The reduction wears off at end of turn")
    void debuffWearsOffAtEndOfTurn() {
        Permanent target = addCreature(player2, new AvatarOfMight());

        castBileBlight(target.getId());
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(5);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(8);
    }

    @Test
    @DisplayName("Lethal reduction puts every same-name creature into its owner's graveyard")
    void killsAllSameNameCreatures() {
        addCreature(player1, new GrizzlyBears());
        Permanent target = addCreature(player2, new GrizzlyBears());
        addCreature(player2, new GrizzlyBears());
        addCreature(player2, new LlanowarElves());

        castBileBlight(target.getId());

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new BileBlight()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castBileBlight(UUID targetId) {
        harness.setHand(player1, List.of(new BileBlight()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.BLACK, 2);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
    }

    private Permanent addCreature(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
