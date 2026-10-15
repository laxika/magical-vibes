package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BambooGroveArcher;
import com.github.laxika.magicalvibes.cards.e.EnchantedEvening;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheRealityChip.class, Forest.class, GrizzlyBears.class, BambooGroveArcher.class,
        EnchantedEvening.class})
class TheRealityChipTest extends BaseCardTest {

    @Test
    void attachedChipPlaysLandFromLibraryTop() {
        Permanent chip = addReadyChip(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        chip.setAttachedTo(creature.getId());
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromLibraryTop(player1);

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isEqualTo(1);
    }

    @Test
    void attachedChipCastsSpellFromLibraryTop() {
        Permanent chip = addReadyChip(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        chip.setAttachedTo(creature.getId());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bears));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveFromLibraryTop(player1);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(bears);
    }

    @Test
    void unattachedChipCannotCastSpellFromLibraryTop() {
        addReadyChip(player1);
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bears));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(bears);
    }

    @Test
    void reconfigureAttachesAndUnattachesTheChip() {
        Permanent chip = addReadyChip(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(chip.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.isCreature(gd, chip)).isFalse();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(chip.getAttachedTo()).isNull();
        assertThat(gqs.isCreature(gd, chip)).isTrue();
    }

    @Test
    void reconfigureCannotTargetAnOpponentsCreature() {
        Permanent chip = addReadyChip(player1);
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(chip.getAttachedTo()).isNull();
    }

    private Permanent addReadyChip(Player player) {
        return addCreatureReady(player, new TheRealityChip());
    }

    @Test
    void unattachedChipPrivatelyShowsItsControllersLibraryTop() {
        addReadyChip(player1);
        Forest top = new Forest();
        harness.setLibrary(player1, List.of(top));
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn1().getSentMessages())
                .anyMatch(message -> message.contains("\"revealedLibraryTopCards\":[[{")
                        && message.contains(top.getId().toString()));
        assertThat(harness.getConn2().getSentMessages())
                .noneMatch(message -> message.contains(top.getId().toString()));
    }

    @Test
    void unattachedChipCannotPlayLandFromLibraryTop() {
        addReadyChip(player1);
        Forest top = new Forest();
        harness.setLibrary(player1, List.of(top));

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
    }

    @Test
    void attachedChipDoesNotGrantAnAdditionalLandPlay() {
        Permanent chip = addReadyChip(player1);
        Permanent host = addCreatureReady(player1, new BambooGroveArcher());
        chip.setAttachedTo(host.getId());
        Forest first = new Forest();
        Forest second = new Forest();
        harness.setLibrary(player1, List.of(first, second));
        harness.castFromLibraryTop(player1);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second);
    }

    @Test
    void reconfigureCannotBeActivatedDuringCombat() {
        Permanent chip = addReadyChip(player1);
        Permanent host = addCreatureReady(player1, new BambooGroveArcher());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, host.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(chip.getAttachedTo()).isNull();
    }

    @Test
    void unattachCannotBeActivatedWhileUnattached() {
        addReadyChip(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void attachedChipStillRequiresManaToCastFromLibraryTop() {
        Permanent chip = addReadyChip(player1);
        Permanent host = addCreatureReady(player1, new BambooGroveArcher());
        chip.setAttachedTo(host.getId());
        BambooGroveArcher top = new BambooGroveArcher();
        harness.setLibrary(player1, List.of(top));

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
    }

    @Test
    void attachedChipDoesNotAllowCreatureSpellsDuringCombat() {
        Permanent chip = addReadyChip(player1);
        Permanent host = addCreatureReady(player1, new BambooGroveArcher());
        chip.setAttachedTo(host.getId());
        BambooGroveArcher top = new BambooGroveArcher();
        harness.setLibrary(player1, List.of(top));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
    }

    @Test
    void unattachingRemovesLibraryPlayPermission() {
        Permanent chip = addReadyChip(player1);
        Permanent host = addCreatureReady(player1, new BambooGroveArcher());
        chip.setAttachedTo(host.getId());
        Forest top = new Forest();
        harness.setLibrary(player1, List.of(top));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(chip.getAttachedTo()).isNull();
        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
    }

    @Test
    void reconfigurePreservesOtherCardTypes() {
        harness.addToBattlefield(player1, new EnchantedEvening());
        Permanent chip = addReadyChip(player1);
        Permanent host = addCreatureReady(player1, new BambooGroveArcher());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 1, 0, null, host.getId());
        harness.passBothPriorities();

        assertThat(chip.getAttachedTo()).isEqualTo(host.getId());
        assertThat(gqs.isCreature(gd, chip)).isFalse();
        assertThat(gqs.isEnchantment(gd, chip)).isTrue();
    }
}
