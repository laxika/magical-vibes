package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HowlingChorus;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.w.WerewolfOfAncientHunger;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SoulSeparator.class, GrizzlyBears.class, Plains.class, SageOfAncientLore.class,
        WerewolfOfAncientHunger.class, ShrillHowler.class, HowlingChorus.class, SpellQueller.class})
class SoulSeparatorTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles the creature card and creates a 1/1 flying Spirit copy plus a stat-matched black Zombie")
    void createsSpiritCopyAndZombieToken() {
        Permanent separator = harness.addToBattlefieldAndReturn(player1, new SoulSeparator());
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(separator);
        harness.activateAbilityWithGraveyardTargets(player1, idx, 0, List.of(bears.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(c -> c.getId().equals(bears.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId())).anyMatch(c -> c.getId().equals(bears.getId()));

        Permanent spirit = findPermanent(player1, "Grizzly Bears");
        assertThat(spirit.getCard().isToken()).isTrue();
        assertThat(spirit.getCard().getPower()).isEqualTo(1);
        assertThat(spirit.getCard().getToughness()).isEqualTo(1);
        assertThat(spirit.getCard().getSubtypes()).contains(CardSubtype.BEAR, CardSubtype.SPIRIT);
        assertThat(spirit.getCard().getKeywords()).contains(Keyword.FLYING);

        Permanent zombie = findPermanent(player1, "Zombie");
        assertThat(zombie.getCard().isToken()).isTrue();
        assertThat(zombie.getCard().getPower()).isEqualTo(2);
        assertThat(zombie.getCard().getToughness()).isEqualTo(2);
        assertThat(zombie.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(zombie.getCard().getSubtypes()).contains(CardSubtype.ZOMBIE);
    }

    @Test
    @DisplayName("Activating sacrifices Soul Separator as a cost")
    void activationSacrificesSource() {
        Permanent separator = harness.addToBattlefieldAndReturn(player1, new SoulSeparator());
        Card separatorCard = separator.getCard();
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(separator);
        harness.activateAbilityWithGraveyardTargets(player1, idx, 0, List.of(bears.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getId().equals(separatorCard.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(separatorCard.getId()));
    }

    @Test
    @DisplayName("Rejects a noncreature card in the graveyard as a target")
    void rejectsNonCreatureTarget() {
        Permanent separator = harness.addToBattlefieldAndReturn(player1, new SoulSeparator());
        Card land = new Plains();
        harness.setGraveyard(player1, List.of(land));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(separator);
        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, idx, 0, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Rejects a creature card in an opponent's graveyard")
    void rejectsOpponentGraveyardTarget() {
        Permanent separator = harness.addToBattlefieldAndReturn(player1, new SoulSeparator());
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(bears));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(separator);
        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, idx, 0, List.of(bears.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Creates no tokens if the targeted creature leaves the graveyard before resolution")
    void removedTargetCreatesNoTokens() {
        harness.addToBattlefield(player1, new SoulSeparator());
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(bears.getId()));

        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(bears));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate a tapped Soul Separator")
    void tappedSourceCannotActivate() {
        Permanent separator = harness.addToBattlefieldAndReturn(player1, new SoulSeparator());
        separator.tap();
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(bears.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Soul Separator");
    }

    @Test
    @DisplayName("Requires five mana to activate")
    void insufficientManaCannotActivate() {
        harness.addToBattlefield(player1, new SoulSeparator());
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(bears.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Soul Separator");
    }

    @Test
    @DisplayName("Zombie uses graveyard characteristic-defined stats while the Spirit is 1/1 and retains its enters ability")
    void characteristicDefinedStatsAndCopiedEnterAbility() {
        harness.addToBattlefield(player1, new SoulSeparator());
        Card sage = new SageOfAncientLore();
        harness.setGraveyard(player1, List.of(sage));
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Plains(), new Plains()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(sage.getId()));
        harness.passBothPriorities();

        Permanent spirit = findPermanent(player1, "Sage of Ancient Lore");
        assertThat(gqs.getEffectivePower(gd, spirit)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, spirit)).isEqualTo(1);
        Permanent zombie = findPermanent(player1, "Zombie");
        assertThat(gqs.getEffectivePower(gd, zombie)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, zombie)).isEqualTo(3);

        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gqs.getEffectivePower(gd, zombie)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, zombie)).isEqualTo(3);
    }

    @Test
    @DisplayName("A Spirit copy of a transforming card can transform and keeps the copy exceptions on its back face")
    void spiritCopyCanTransform() {
        harness.addToBattlefield(player1, new SoulSeparator());
        Card howler = new ShrillHowler();
        harness.setGraveyard(player1, List.of(howler));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(howler.getId()));
        harness.passBothPriorities();

        Permanent spirit = findPermanent(player1, "Shrill Howler");
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.GREEN, 1);
        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(spirit);
        harness.activateAbility(player1, idx, 0, null, null);
        harness.passBothPriorities();

        assertThat(spirit.isTransformed()).isTrue();
        assertThat(spirit.getCard().getName()).isEqualTo("Howling Chorus");
        assertThat(gqs.getEffectivePower(gd, spirit)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, spirit)).isEqualTo(1);
        assertThat(spirit.getCard().getSubtypes()).contains(CardSubtype.SPIRIT);
        assertThat(spirit.getCard().getKeywords()).contains(Keyword.FLYING);
    }

    @Test
    @DisplayName("The Spirit copy retains every color of a multicolored creature")
    void spiritCopyRetainsMultipleColors() {
        harness.addToBattlefield(player1, new SoulSeparator());
        Card queller = new SpellQueller();
        harness.setGraveyard(player1, List.of(queller));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(queller.getId()));
        harness.passBothPriorities();

        Permanent spirit = findPermanent(player1, "Spell Queller");
        assertThat(gqs.getEffectiveColors(gd, spirit))
                .containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLUE);
    }
}
