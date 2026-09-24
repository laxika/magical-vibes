package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.j.JackOLantern;
import com.github.laxika.magicalvibes.cards.m.MikokoroCenterOfTheSea;
import com.github.laxika.magicalvibes.cards.s.SakuraTribeScout;
import com.github.laxika.magicalvibes.cards.s.ShinenOfFurysFire;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PithingNeedle.class, SakuraTribeScout.class, MikokoroCenterOfTheSea.class,
        ShinenOfFurysFire.class, JackOLantern.class})
class PithingNeedleTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Pithing Needle puts it on the stack as artifact spell")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new PithingNeedle()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Pithing Needle");
    }

    @Test
    @DisplayName("Resolving Pithing Needle awaits card name choice before entering battlefield")
    void resolvingTriggersCardNameChoice() {
        harness.setHand(player1, List.of(new PithingNeedle()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        // As this artifact enters, its name choice must be made before it enters.
        harness.assertNotOnBattlefield(player1, "Pithing Needle");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Choosing a card name sets chosenName on the permanent")
    void choosingNameSetsOnPermanent() {
        harness.setHand(player1, List.of(new PithingNeedle()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Sakura-Tribe Scout");

        Permanent perm = findPermanent(player1, "Pithing Needle");
        assertThat(perm.getChosenName()).isEqualTo("Sakura-Tribe Scout");
    }

    @Test
    @DisplayName("Card name choice clears awaiting state")
    void cardNameChoiceClearsAwaitingState() {
        harness.setHand(player1, List.of(new PithingNeedle()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Sakura-Tribe Scout");

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNull();
    }

    @Test
    @DisplayName("Card name choice is logged")
    void cardNameChoiceIsLogged() {
        harness.setHand(player1, List.of(new PithingNeedle()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Sakura-Tribe Scout");

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log ->
                log.contains("Sakura-Tribe Scout") && log.contains("Pithing Needle"));
    }

    @Test
    @DisplayName("Blocks non-mana activated abilities of the named card")
    void blocksNonManaActivatedAbilities() {
        addReadyPithingNeedle(player1, "Sakura-Tribe Scout");
        addCreatureReady(player2, new SakuraTribeScout());

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated")
                .hasMessageContaining("Pithing Needle");
    }

    @Test
    @DisplayName("Does NOT block mana abilities of the named card")
    void doesNotBlockManaAbilities() {
        addReadyPithingNeedle(player1, "Mikokoro, Center of the Sea");
        Permanent mikokoro = harness.addToBattlefieldAndReturn(player2, new MikokoroCenterOfTheSea());
        int manaBefore = gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS);

        harness.activateAbility(player2, 0, null, null);

        assertThat(mikokoro.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS))
                .isEqualTo(manaBefore + 1);
    }

    @Test
    @DisplayName("Does NOT block abilities of differently-named cards")
    void doesNotBlockDifferentlyNamedCards() {
        addReadyPithingNeedle(player1, "Mikokoro, Center of the Sea");
        addCreatureReady(player2, new SakuraTribeScout());

        harness.activateAbility(player2, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Sakura-Tribe Scout");
    }

    @Test
    @DisplayName("Blocks abilities of the controller's own named cards")
    void blocksOwnCardsAbilities() {
        addReadyPithingNeedle(player1, "Sakura-Tribe Scout");
        addCreatureReady(player1, new SakuraTribeScout());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    @Test
    @DisplayName("Blocks a named activated ability from a card in hand")
    void blocksHandActivatedAbility() {
        addReadyPithingNeedle(player1, "Shinen of Fury's Fire");
        Permanent target = addCreatureReady(player2, new SakuraTribeScout());
        harness.setHand(player2, List.of(new ShinenOfFurysFire()));
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player2, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated")
                .hasMessageContaining("Pithing Needle");
    }

    @Test
    @DisplayName("After Pithing Needle leaves the battlefield, abilities are usable again")
    void abilitiesWorkAfterNeedleRemoved() {
        Permanent needle = addReadyPithingNeedle(player1, "Sakura-Tribe Scout");
        addCreatureReady(player2, new SakuraTribeScout());

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");

        gd.playerBattlefields.get(player1.getId()).remove(needle);

        harness.activateAbility(player2, 0, null, null);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Multiple Pithing Needles can name different cards")
    void multiplePithingNeedlesBlockDifferentCards() {
        addReadyPithingNeedle(player1, "Sakura-Tribe Scout");
        addReadyPithingNeedle(player1, "Mikokoro, Center of the Sea");
        addCreatureReady(player2, new SakuraTribeScout());
        harness.addToBattlefield(player2, new MikokoroCenterOfTheSea());

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");

        harness.addMana(player2, ManaColor.COLORLESS, 2);
        assertThatThrownBy(() -> harness.activateAbility(player2, 1, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    @Test
    @DisplayName("Does NOT block a named mana ability from a card in the graveyard")
    void doesNotBlockGraveyardManaAbility() {
        addReadyPithingNeedle(player1, "Jack-o'-Lantern");
        harness.setGraveyard(player2, List.of(new JackOLantern()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateGraveyardAbility(player2, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Jack-o'-Lantern");
    }

    @Test
    @DisplayName("Pithing Needle with no chosen name does not block anything")
    void noChosenNameDoesNotBlock() {
        harness.addToBattlefield(player1, new PithingNeedle());
        addCreatureReady(player2, new SakuraTribeScout());

        harness.activateAbility(player2, 0, null, null);
        assertThat(gd.stack).hasSize(1);
    }

    private Permanent addReadyPithingNeedle(Player player, String chosenName) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new PithingNeedle());
        perm.setChosenName(chosenName);
        return perm;
    }
}
