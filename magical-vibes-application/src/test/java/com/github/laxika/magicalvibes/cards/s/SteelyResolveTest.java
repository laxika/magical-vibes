package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.g.GoblinSkyRaider;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SteelyResolve.class, ElvishWarrior.class, GoblinSkyRaider.class, Smother.class,
        Naturalize.class, Standardize.class, Opalescence.class})
class SteelyResolveTest extends BaseCardTest {

    @Test
    @DisplayName("Choosing a creature type gives matching creatures shroud")
    void grantsShroudToCreaturesOfChosenType() {
        Permanent ownElf = harness.addToBattlefieldAndReturn(player1, new ElvishWarrior());
        Permanent ownGoblin = harness.addToBattlefieldAndReturn(player1, new GoblinSkyRaider());
        Permanent opponentElf = harness.addToBattlefieldAndReturn(player2, new ElvishWarrior());

        castSteelyResolveChoosingElf();

        assertThat(gqs.hasKeyword(gd, ownElf, Keyword.SHROUD)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownGoblin, Keyword.SHROUD)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentElf, Keyword.SHROUD)).isTrue();

        Permanent laterElf = harness.enterBattlefieldAndReturn(player1, new ElvishWarrior());
        Permanent laterGoblin = harness.enterBattlefieldAndReturn(player1, new GoblinSkyRaider());

        assertThat(gqs.hasKeyword(gd, laterElf, Keyword.SHROUD)).isTrue();
        assertThat(gqs.hasKeyword(gd, laterGoblin, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("Shroud prevents spells from targeting creatures of the chosen type")
    void chosenTypeCreaturesCannotBeTargeted() {
        Permanent elf = harness.addToBattlefieldAndReturn(player2, new ElvishWarrior());
        Permanent goblin = harness.addToBattlefieldAndReturn(player2, new GoblinSkyRaider());

        castSteelyResolveChoosingElf();

        harness.setHand(player1, List.of(new Smother()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, elf.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");

        harness.castAndResolveInstant(player1, 0, goblin.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(goblin);
    }

    @Test
    @DisplayName("Shroud also prevents the controller from targeting their own creatures")
    void ownChosenTypeCreatureCannotBeTargeted() {
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new ElvishWarrior());
        castSteelyResolveChoosingElf();

        harness.setHand(player1, List.of(new Smother()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, elf.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    @DisplayName("Removing Steely Resolve removes its shroud grant")
    void removingEnchantmentAllowsTargetingAgain() {
        Permanent elf = harness.addToBattlefieldAndReturn(player2, new ElvishWarrior());
        castSteelyResolveChoosingElf();

        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Steely Resolve"));

        assertThat(gqs.hasKeyword(gd, elf, Keyword.SHROUD)).isFalse();
        harness.setHand(player1, List.of(new Smother()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, elf.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(elf);
    }

    @Test
    @DisplayName("Multiple copies remember independent creature type choices")
    void copiesHaveIndependentChoices() {
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new ElvishWarrior());
        Permanent goblin = harness.addToBattlefieldAndReturn(player2, new GoblinSkyRaider());
        castSteelyResolveChoosingElf();
        java.util.UUID firstResolveId = harness.getPermanentId(player1, "Steely Resolve");

        harness.castFromHand(player1, new SteelyResolve(), "{1}{G}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.GOBLIN.name());

        assertThat(gqs.hasKeyword(gd, elf, Keyword.SHROUD)).isTrue();
        assertThat(gqs.hasKeyword(gd, goblin, Keyword.SHROUD)).isTrue();

        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, firstResolveId);

        assertThat(gqs.hasKeyword(gd, elf, Keyword.SHROUD)).isFalse();
        assertThat(gqs.hasKeyword(gd, goblin, Keyword.SHROUD)).isTrue();
    }

    @Test
    @DisplayName("Steely Resolve grants itself shroud when it becomes a creature of the chosen type")
    void animatedResolveOfChosenTypeHasShroud() {
        castSteelyResolveChoosingElf();
        Permanent resolve = findPermanent(player1, "Steely Resolve");
        harness.addToBattlefield(player1, new Opalescence());

        harness.castFromHand(player1, new Standardize(), "{U}{U}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.ELF.name());

        assertThat(gqs.isCreature(gd, resolve)).isTrue();
        assertThat(gqs.effectiveCreatureSubtypes(gd, resolve)).contains(CardSubtype.ELF);
        assertThat(gqs.hasKeyword(gd, resolve, Keyword.SHROUD)).isTrue();
    }

    private void castSteelyResolveChoosingElf() {
        harness.castFromHand(player1, new SteelyResolve(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, CardSubtype.ELF.name());
    }
}
