package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.a.AccordersShield;
import com.github.laxika.magicalvibes.cards.g.GoldMyr;
import com.github.laxika.magicalvibes.cards.s.Shatter;
import com.github.laxika.magicalvibes.cards.y.YellowjacketHeartlessMarauder;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UltronArtificialMalevolence.class, AccordersShield.class, GoldMyr.class,
        Shatter.class, YellowjacketHeartlessMarauder.class})
class UltronArtificialMalevolenceTest extends BaseCardTest {

    @Test
    void noncreatureArtifactCopyBecomesRobotVillainCreature() {
        addUltronReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setHand(player1, List.of(new AccordersShield()));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent token = findToken(player1, "Accorder's Shield");
        assertThat(token.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(gqs.isCreature(gd, token)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, token, CardSubtype.ROBOT)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, token, CardSubtype.VILLAIN)).isTrue();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
    }

    @Test
    void creatureArtifactCopyKeepsItsCopiedCharacteristics() {
        addUltronReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.setHand(player1, List.of(new GoldMyr()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent token = findToken(player1, "Gold Myr");
        assertThat(token.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(token.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(token.getCard().getSubtypes())
                .doesNotContain(CardSubtype.ROBOT, CardSubtype.VILLAIN);
    }

    private Permanent addUltronReady(Player player) {
        return addCreatureReady(player, new UltronArtificialMalevolence());
    }

    @Test
    void noncreatureCopyDoesNotEnterAsAVillainCreature() {
        addUltronReady(player1);
        Permanent yellowjacket = harness.addToBattlefieldAndReturn(player1,
                new YellowjacketHeartlessMarauder());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setHand(player1, List.of(new AccordersShield()));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        if (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(gqs.isCreature(gd, findToken(player1, "Accorder's Shield"))).isTrue();
        assertThat(yellowjacket.getPowerModifier()).isZero();
    }

    @Test
    void copiesArtifactUsingLastKnownInformationAfterItIsDestroyed() {
        addUltronReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new AccordersShield(), new Shatter()));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        Permanent original = findPermanent(player1, "Accorder's Shield");
        harness.castAndResolveInstant(player1, 0, original.getId());
        harness.assertInGraveyard(player1, "Accorder's Shield");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findToken(player1, "Accorder's Shield")).isNotNull();
    }

    @Test
    void decliningPaymentCreatesNoToken() {
        addUltronReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setHand(player1, List.of(new AccordersShield()));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(countPermanents(player1, "Accorder's Shield")).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotTriggerForItsOwnEntry() {
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.setHand(player1, List.of(new UltronArtificialMalevolence()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Ultron, Artificial Malevolence");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void doesNotTriggerForOpponentsArtifact() {
        addUltronReady(player1);
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new AccordersShield()));

        harness.castArtifact(player2, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Accorder's Shield");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private Permanent findToken(Player player, String name) {
        return findPermanents(player, name).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
    }
}
