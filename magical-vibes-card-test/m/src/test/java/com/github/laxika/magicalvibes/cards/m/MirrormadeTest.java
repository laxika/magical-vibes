package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AllThatGlitters;
import com.github.laxika.magicalvibes.cards.g.Gingerbrute;
import com.github.laxika.magicalvibes.cards.g.GoldenEgg;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JayemdaeTome;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Mirrormade.class, GloriousAnthem.class, GrizzlyBears.class, JayemdaeTome.class,
        AllThatGlitters.class, Gingerbrute.class, GoldenEgg.class})
class MirrormadeTest extends BaseCardTest {

    @Test
    @DisplayName("Mirrormade copies an artifact")
    void copiesArtifact() {
        Permanent tome = harness.addToBattlefieldAndReturn(player2, new JayemdaeTome());
        castMirrormade();

        chooseCopy(tome);

        Permanent copy = findPermanent(player1, "Jayemdae Tome");
        assertThat(copy.getOriginalCard().getName()).isEqualTo("Mirrormade");
        assertThat(copy.getCard().getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(copy.getCard().getActivatedAbilities()).hasSize(1);
    }

    @Test
    @DisplayName("Mirrormade copies an enchantment and its static effect")
    void copiesEnchantment() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent anthem = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());
        castMirrormade();

        chooseCopy(anthem);

        Permanent copy = findPermanent(player1, "Glorious Anthem");
        assertThat(copy.getOriginalCard().getName()).isEqualTo("Mirrormade");
        assertThat(copy.getCard().getType()).isEqualTo(CardType.ENCHANTMENT);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("Mirrormade does not offer a copy choice without an artifact or enchantment")
    void doesNotOfferCopyChoiceWithoutValidPermanent() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castMirrormade();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Mirrormade");
    }

    @Test
    @DisplayName("Mirrormade can decline to copy a valid permanent")
    void canDeclineCopy() {
        harness.addToBattlefield(player2, new GoldenEgg());
        castMirrormade();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Mirrormade");
        harness.assertNotOnBattlefield(player1, "Golden Egg");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Mirrormade copies an artifact creature without copying its tapped status")
    void copiesArtifactCreatureWithoutTappedStatus() {
        Permanent original = harness.addToBattlefieldAndReturn(player2, new Gingerbrute());
        original.tap();
        castMirrormade();

        chooseCopy(original);

        Permanent copy = findPermanent(player1, "Gingerbrute");
        assertThat(copy.isTapped()).isFalse();
        assertThat(original.isTapped()).isTrue();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.assertLife(player1, 23);
        harness.assertInGraveyard(player1, "Mirrormade");
        harness.assertNotOnBattlefield(player1, "Gingerbrute");
    }

    @Test
    @DisplayName("Mirrormade triggers the copied artifact's enter ability")
    void triggersCopiedEnterAbility() {
        Permanent egg = harness.addToBattlefieldAndReturn(player2, new GoldenEgg());
        Gingerbrute drawnCard = new Gingerbrute();
        harness.setLibrary(player1, List.of(drawnCard));
        castMirrormade();

        chooseCopy(egg);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Golden Egg");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    @DisplayName("Mirrormade copies the current copiable values of another Mirrormade")
    void copiesExistingCopy() {
        Permanent egg = harness.addToBattlefieldAndReturn(player2, new GoldenEgg());
        harness.setLibrary(player1, List.of(new Gingerbrute(), new Gingerbrute()));
        castMirrormade();
        chooseCopy(egg);
        harness.passBothPriorities();
        Permanent firstCopy = findPermanent(player1, "Golden Egg");

        castMirrormade();
        chooseCopy(firstCopy);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2)
                .allSatisfy(copy -> assertThat(copy.getOriginalCard().getName()).isEqualTo("Mirrormade"));
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Mirrormade chooses a recipient before entering as a copy of an Aura")
    void choosesRecipientForCopiedAura() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new Gingerbrute());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new AllThatGlitters());
        aura.setAttachedTo(creature.getId());
        castMirrormade();

        chooseCopy(aura);

        PendingInteraction.PermanentChoice recipientChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(recipientChoice).isNotNull();
        assertThat(recipientChoice.validPermanentIds()).contains(creature.getId());
        harness.handlePermanentChosen(player1, creature.getId());

        Permanent copy = findPermanent(player1, "All That Glitters");
        assertThat(copy.getAttachedTo()).isEqualTo(creature.getId());
        harness.assertNotInGraveyard(player1, "Mirrormade");
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
    }

    private void castMirrormade() {
        harness.castFromHand(player1, new Mirrormade(), "{1}{U}{U}");
        harness.passBothPriorities();
    }

    private void chooseCopy(Permanent target) {
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, target.getId());
    }
}
