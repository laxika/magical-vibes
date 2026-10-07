package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.a.AegisAutomaton;
import com.github.laxika.magicalvibes.cards.d.Disallow;
import com.github.laxika.magicalvibes.cards.i.IrontreadCrusher;
import com.github.laxika.magicalvibes.cards.m.MobileGarrison;
import com.github.laxika.magicalvibes.cards.t.TezzeretsTouch;
import com.github.laxika.magicalvibes.cards.w.WalkingBallista;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

@CardUsed({SramsExpertise.class, GrizzlyBears.class, SerraAngel.class, AegisAutomaton.class,
        Disallow.class, IrontreadCrusher.class, MobileGarrison.class, SpireOfIndustry.class,
        TezzeretsTouch.class, WalkingBallista.class})
class SramsExpertiseTest extends BaseCardTest {

    @Test
    @DisplayName("Cast creates three 1/1 colorless Servo artifact creature tokens")
    void createsThreeServoTokens() {
        castExpertise(List.of(new SramsExpertise()));

        assertThat(servos()).hasSize(3);
        assertThat(servos()).allSatisfy(servo -> {
            assertThat(servo.getCard().hasType(CardType.ARTIFACT)).isTrue();
            assertThat(servo.getCard().hasType(CardType.CREATURE)).isTrue();
            assertThat(servo.getCard().getSubtypes()).contains(CardSubtype.SERVO);
            assertThat(servo.getEffectivePower()).isEqualTo(1);
            assertThat(servo.getEffectiveToughness()).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("Cast offers a spell with mana value three or less from hand for free")
    void castsLowManaValueSpellFromHand() {
        GrizzlyBears bears = new GrizzlyBears();
        castExpertise(List.of(new SramsExpertise(), bears));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(bears.getId()));
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(bears.getId()));
    }

    @Test
    @DisplayName("Cast does not offer a spell with mana value greater than three")
    void doesNotOfferHighManaValueSpell() {
        castExpertise(List.of(new SramsExpertise(), new SerraAngel()));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Declining the free spell keeps it in hand and still creates the Servos")
    void canDeclineFreeSpell() {
        castExpertise(List.of(new SramsExpertise(), new AegisAutomaton()));

        harness.handleMayAbilityChosen(player1, false);

        assertThat(servos()).hasSize(3);
        harness.assertInHand(player1, "Aegis Automaton");
        harness.assertNotOnBattlefield(player1, "Aegis Automaton");
        harness.assertInGraveyard(player1, "Sram's Expertise");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Only one eligible spell can be cast for free")
    void castsOnlyOneSpell() {
        castExpertise(List.of(new SramsExpertise(), new AegisAutomaton(), new MobileGarrison()));

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.assertInHand(player1, "Mobile Garrison");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Aegis Automaton");
        harness.assertNotOnBattlefield(player1, "Mobile Garrison");
    }

    @Test
    @DisplayName("A noncreature spell with mana value exactly three is cast for free")
    void castsManaValueThreeArtifact() {
        castExpertise(List.of(new SramsExpertise(), new MobileGarrison()));

        assertThat(servos()).hasSize(3);
        harness.handleMayAbilityChosen(player1, true);
        harness.assertNotOnBattlefield(player1, "Mobile Garrison");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Mobile Garrison");
        harness.assertNotInHand(player1, "Mobile Garrison");
    }

    @Test
    @DisplayName("A spell with mana value exactly four is ineligible")
    void excludesManaValueFourSpell() {
        castExpertise(List.of(new SramsExpertise(), new IrontreadCrusher()));

        assertThat(servos()).hasSize(3);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Irontread Crusher");
    }

    @Test
    @DisplayName("The free counterspell can target the resolving Expertise")
    void freeSpellCanTargetResolvingExpertise() {
        SramsExpertise expertise = new SramsExpertise();
        castExpertise(List.of(expertise, new Disallow()));

        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(expertise.getId());
        harness.handlePermanentChosen(player1, expertise.getId());
        harness.assertInGraveyard(player1, "Sram's Expertise");
        harness.assertNotInHand(player1, "Disallow");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Disallow");
        assertThat(servos()).hasSize(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A land is not eligible for the free spell")
    void cannotPlayLandWithExpertise() {
        castExpertise(List.of(new SramsExpertise(), new SpireOfIndustry()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInHand(player1, "Spire of Industry");
        harness.assertNotOnBattlefield(player1, "Spire of Industry");
        assertThat(servos()).hasSize(3);
    }

    @Test
    @DisplayName("An X spell is cast with X equal to zero")
    void freeXSpellUsesZero() {
        castExpertise(List.of(new SramsExpertise(), new WalkingBallista()));

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getXValue()).isZero();
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Walking Ballista");
        harness.assertNotOnBattlefield(player1, "Walking Ballista");
        assertThat(servos()).hasSize(3);
    }

    @Test
    @DisplayName("The free Aura can enchant a Servo created by Expertise")
    void freeAuraCanTargetNewServo() {
        castExpertise(List.of(new SramsExpertise(), new TezzeretsTouch()));
        Permanent servo = servos().getFirst();

        harness.handleMayAbilityChosen(player1, true);
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(servo.getId());
        harness.handlePermanentChosen(player1, servo.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Tezzeret's Touch");
        assertThat(gqs.getEffectivePower(gd, servo)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, servo)).isEqualTo(5);
        assertThat(servos()).hasSize(3);
    }

    @Test
    @DisplayName("Servos are colorless, untapped, and controlled only by the caster")
    void createsColorlessServosUnderCasterControl() {
        castExpertise(List.of(new SramsExpertise()));

        assertThat(servos()).hasSize(3).allSatisfy(servo -> {
            assertThat(servo.getCard().getColors()).isEmpty();
            assertThat(servo.isTapped()).isFalse();
        });
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    private void castExpertise(List<Card> hand) {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, hand);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player1, 0, List.of());
    }

    private List<Permanent> servos() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.SERVO))
                .toList();
    }
}
