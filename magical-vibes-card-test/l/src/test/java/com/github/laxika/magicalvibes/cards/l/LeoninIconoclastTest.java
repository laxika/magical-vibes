package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.AegisOfTheGods;
import com.github.laxika.magicalvibes.cards.a.AjanisPresence;
import com.github.laxika.magicalvibes.cards.f.FontOfVigor;
import com.github.laxika.magicalvibes.cards.g.GoldenHind;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LeoninIconoclast.class, AegisOfTheGods.class, AjanisPresence.class,
        FontOfVigor.class, GoldenHind.class})
class LeoninIconoclastTest extends BaseCardTest {

    @Test
    @DisplayName("Heroic destroys an enchantment creature an opponent controls")
    void heroicDestroysEnchantmentCreatureOpponentControls() {
        harness.addToBattlefield(player1, new LeoninIconoclast());
        Permanent enchantmentCreature = harness.addToBattlefieldAndReturn(player2, new AegisOfTheGods());
        harness.setHand(player1, List.of(new AjanisPresence()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID iconoclastId = harness.getPermanentId(player1, "Leonin Iconoclast");
        harness.castInstant(player1, 0, iconoclastId);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, enchantmentCreature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(enchantmentCreature);
    }

    @Test
    @DisplayName("Heroic cannot target a non-enchantment creature")
    void heroicCannotTargetNonEnchantmentCreature() {
        harness.addToBattlefield(player1, new LeoninIconoclast());
        Permanent enchantmentCreature = harness.addToBattlefieldAndReturn(player2, new AegisOfTheGods());
        Permanent nonEnchantmentCreature = harness.addToBattlefieldAndReturn(player2, new GoldenHind());
        harness.setHand(player1, List.of(new AjanisPresence()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID iconoclastId = harness.getPermanentId(player1, "Leonin Iconoclast");
        harness.castInstant(player1, 0, iconoclastId);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, nonEnchantmentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, enchantmentCreature.getId());
    }

    @Test
    @DisplayName("An opponent's spell targeting Leonin Iconoclast does not trigger heroic")
    void opponentsSpellDoesNotTriggerHeroic() {
        harness.addToBattlefield(player1, new LeoninIconoclast());
        harness.addToBattlefield(player2, new AegisOfTheGods());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new AjanisPresence()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        UUID iconoclastId = harness.getPermanentId(player1, "Leonin Iconoclast");
        harness.castInstant(player2, 0, iconoclastId);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    @Test
    @DisplayName("Heroic cannot target your own enchantment creature or a noncreature enchantment")
    void heroicRejectsOwnCreatureAndNoncreatureEnchantment() {
        Permanent iconoclast = harness.addToBattlefieldAndReturn(player1, new LeoninIconoclast());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new AegisOfTheGods());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new FontOfVigor());
        Permanent legalTarget = harness.addToBattlefieldAndReturn(player2, new AegisOfTheGods());
        harness.setHand(player1, List.of(new AjanisPresence()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, iconoclast.getId());

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, enchantment.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, legalTarget.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Aegis of the Gods");
        harness.assertOnBattlefield(player1, "Aegis of the Gods");
        harness.assertOnBattlefield(player2, "Font of Vigor");
    }

    @Test
    @DisplayName("A spell targeting another creature does not trigger heroic")
    void spellTargetingAnotherCreatureDoesNotTriggerHeroic() {
        harness.addToBattlefield(player1, new LeoninIconoclast());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new GoldenHind());
        harness.addToBattlefield(player2, new AegisOfTheGods());
        harness.setHand(player1, List.of(new AjanisPresence()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, otherCreature.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        harness.assertOnBattlefield(player2, "Aegis of the Gods");
        harness.assertInGraveyard(player1, "Ajani's Presence");
    }

    @Test
    @DisplayName("A spell targeting Leonin Iconoclast with no legal heroic target still resolves")
    void spellResolvesWithoutLegalHeroicTarget() {
        Permanent iconoclast = harness.addToBattlefieldAndReturn(player1, new LeoninIconoclast());
        harness.addToBattlefield(player1, new AegisOfTheGods());
        harness.addToBattlefield(player2, new FontOfVigor());
        harness.setHand(player1, List.of(new AjanisPresence()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, iconoclast.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Ajani's Presence");
        harness.assertOnBattlefield(player1, "Aegis of the Gods");
        harness.assertOnBattlefield(player2, "Font of Vigor");
    }

    @Test
    @DisplayName("A spell targeting Iconoclast and another creature triggers heroic once")
    void multitargetSpellTriggersHeroicOnce() {
        Permanent iconoclast = harness.addToBattlefieldAndReturn(player1, new LeoninIconoclast());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new GoldenHind());
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player2, new AegisOfTheGods());
        Permanent secondTarget = harness.addToBattlefieldAndReturn(player2, new AegisOfTheGods());
        harness.setHand(player1, List.of(new AjanisPresence()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, List.of(iconoclast.getId(), otherCreature.getId()));
        harness.handlePermanentChosen(player1, firstTarget.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(firstTarget).contains(secondTarget);
        harness.assertInGraveyard(player2, "Aegis of the Gods");
    }

    @Test
    @DisplayName("An enchantment creature made indestructible in response survives heroic")
    void indestructibleTargetSurvivesHeroic() {
        Permanent iconoclast = harness.addToBattlefieldAndReturn(player1, new LeoninIconoclast());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AegisOfTheGods());
        harness.setHand(player1, List.of(new AjanisPresence()));
        harness.setHand(player2, List.of(new AjanisPresence()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, iconoclast.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        harness.assertNotInGraveyard(player2, "Aegis of the Gods");
    }
}
