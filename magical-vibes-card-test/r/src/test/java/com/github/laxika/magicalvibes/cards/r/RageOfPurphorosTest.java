package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RageOfPurphoros.class, AirElemental.class, AvatarOfMight.class, Forest.class})
class RageOfPurphorosTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 4 damage, prevents regeneration, and offers scry 1")
    void dealsDamagePreventsRegenerationAndScries() {
        Permanent target = addCreatureReady(player2, new AvatarOfMight());
        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card originalTop = deck.getFirst();

        castRage(target);

        assertThat(target.isCantRegenerateThisTurn()).isTrue();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(deck.getLast()).isSameAs(originalTop);
        harness.assertInGraveyard(player1, "Rage of Purphoros");
    }

    @Test
    @DisplayName("Lethal damage cannot be stopped by regeneration")
    void lethalDamageCannotBeRegenerated() {
        Permanent target = addCreatureReady(player2, new AirElemental());
        target.setRegenerationShield(1);

        castRage(target);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        harness.assertNotOnBattlefield(player2, "Air Elemental");
        harness.assertInGraveyard(player2, "Air Elemental");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new RageOfPurphoros()));
        harness.addMana(player1, ManaColor.RED, 5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, harness.getPermanentId(player2, "Forest")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Deals exactly four damage to your own creature and can keep the scry card on top")
    void damagesOwnCreatureAndKeepsTopCard() {
        Permanent target = addCreatureReady(player1, new AvatarOfMight());
        Card top = new Forest();
        Card second = new RageOfPurphoros();
        harness.setLibrary(player1, List.of(top, second));

        castRage(target);

        assertThat(target.getMarkedDamage()).isEqualTo(4);
        assertThat(target.isCantRegenerateThisTurn()).isTrue();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top, second);
        harness.assertOnBattlefield(player1, "Avatar of Might");
        harness.assertInGraveyard(player1, "Rage of Purphoros");
    }

    @Test
    @DisplayName("An illegal sole target prevents the entire spell from resolving, including scry")
    void illegalTargetPreventsScry() {
        Permanent target = addCreatureReady(player2, new AirElemental());
        Card top = new Forest();
        harness.setLibrary(player1, List.of(top));
        harness.setHand(player1, List.of(new RageOfPurphoros()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castSorcery(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
        harness.assertInGraveyard(player1, "Rage of Purphoros");
    }

    @Test
    @DisplayName("An empty library does not stop damage or the regeneration restriction")
    void resolvesWithEmptyLibrary() {
        Permanent target = addCreatureReady(player2, new AvatarOfMight());
        harness.setLibrary(player1, List.of());

        castRage(target);

        assertThat(target.getMarkedDamage()).isEqualTo(4);
        assertThat(target.isCantRegenerateThisTurn()).isTrue();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        harness.assertInGraveyard(player1, "Rage of Purphoros");
    }

    private void castRage(Permanent target) {
        harness.setHand(player1, List.of(new RageOfPurphoros()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
