package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.d.DarksteelCitadel;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({UnluckyDrop.class, GloriousAnthem.class, GrizzlyBears.class, Island.class, Spellbook.class,
        DarksteelCitadel.class})
class UnluckyDropTest extends BaseCardTest {

    @Test
    @DisplayName("The target creature's owner can put it on the bottom of their library")
    void targetCreatureOwnerChoosesBottom() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card topCard = new Island();
        harness.setLibrary(player2, List.of(topCard));

        cast(target);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.TargetLibraryDestinationChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.TargetLibraryDestinationChoice.class)
                .playerId()).isEqualTo(player2.getId());

        harness.handleListChoice(player2, "Bottom");

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(topCard, target.getCard());
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Unlucky Drop");
    }

    @Test
    @DisplayName("The target artifact's owner can keep it on top of their library")
    void targetArtifactOwnerChoosesTop() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Spellbook());
        Card topCard = new Island();
        harness.setLibrary(player2, List.of(topCard));

        cast(target);
        harness.handleListChoice(player2, "Top");

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(target.getCard(), topCard);
        harness.assertNotOnBattlefield(player2, "Spellbook");
    }

    @Test
    @DisplayName("Cannot target an enchantment")
    void cannotTargetEnchantment() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());
        harness.setHand(player1, List.of(new UnluckyDrop()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @CardUsed(DarksteelCitadel.class)
    @DisplayName("An artifact land is a legal target and is put into its owner's library")
    void artifactLandCanBePutOnBottom() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DarksteelCitadel());
        Card topCard = new Island();
        harness.setLibrary(player2, List.of(topCard));

        cast(target);
        harness.handleListChoice(player2, "Bottom");

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(topCard, target.getCard());
        harness.assertNotOnBattlefield(player2, "Darksteel Citadel");
    }

    @Test
    @DisplayName("The owner chooses the destination when another player controls the target")
    void ownerChoosesForCreatureControlledByOpponent() {
        Card creature = new GrizzlyBears();
        creature.setOwnerId(player2.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player1, creature);
        Card topCard = new Island();
        harness.setLibrary(player2, List.of(topCard));

        cast(target);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.TargetLibraryDestinationChoice.class)
                .playerId()).isEqualTo(player2.getId());
        assertThatThrownBy(() -> harness.handleListChoice(player1, "Bottom"))
                .isInstanceOf(IllegalStateException.class);
        harness.handleListChoice(player2, "Bottom");

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(topCard, creature);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(creature);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("A target that leaves before resolution causes no destination choice")
    void removedTargetDoesNotPromptForChoice() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card topCard = new Island();
        harness.setLibrary(player2, List.of(topCard));
        harness.setHand(player1, List.of(new UnluckyDrop()));
        addMana();
        harness.castInstant(player1, 0, target.getId());
        harness.getPermanentRemovalService().removePermanentToHand(gd, target);

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(topCard);
        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Unlucky Drop");
    }

    private void cast(Permanent target) {
        harness.setHand(player1, List.of(new UnluckyDrop()));
        addMana();
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

}
