package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.e.EsixFractalBloom;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Hurricane;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
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

@CardUsed({AccessDenied.class, GrizzlyBears.class, SerraAngel.class, EsixFractalBloom.class,
        Hurricane.class, Ornithopter.class})
class AccessDeniedTest extends BaseCardTest {

    @Test
    @DisplayName("Counters the spell and creates one Thopter for each mana in its mana value")
    void countersSpellAndCreatesThoptersForManaValue() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new AccessDenied()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bears.getId());

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(thopters(player2)).hasSize(2);
    }

    @Test
    @DisplayName("Thopter count follows a higher-mana-value spell")
    void thopterCountFollowsHigherManaValueSpell() {
        SerraAngel angel = new SerraAngel();
        harness.setHand(player1, List.of(angel));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.setHand(player2, List.of(new AccessDenied()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, angel.getId());

        harness.assertInGraveyard(player1, "Serra Angel");
        assertThat(thopters(player2)).hasSize(5);
    }

    @Test
    @DisplayName("Created tokens are untapped 1/1 colorless Thopter artifact creatures with flying")
    void createsCorrectThopterTokens() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player2, List.of(new AccessDenied()));
        harness.addMana(player2, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bears.getId());

        assertThat(thopters(player2)).hasSize(2).allSatisfy(token -> {
            assertThat(token.getCard().getName()).isEqualTo("Thopter");
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
            assertThat(token.getCard().hasType(CardType.CREATURE)).isTrue();
            assertThat(token.getCard().hasType(CardType.ARTIFACT)).isTrue();
            assertThat(token.getCard().getSubtypes()).contains(CardSubtype.THOPTER);
            assertThat(token.getCard().getKeywords()).contains(Keyword.FLYING);
            assertThat(token.getCard().getColor()).isNull();
            assertThat(token.isTapped()).isFalse();
        });
        assertThat(thopters(player1)).isEmpty();
    }

    @Test
    @DisplayName("Counters an instant before offering its token creation replacement")
    void countersSpellBeforeTokenReplacementChoice() {
        harness.addToBattlefield(player1, new EsixFractalBloom());
        harness.addToBattlefield(player2, new GrizzlyBears());
        GrizzlyBears bears = new GrizzlyBears();
        AccessDenied opposingCounter = new AccessDenied();
        harness.setHand(player1, List.of(bears, new AccessDenied()));
        harness.setHand(player2, List.of(opposingCounter));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.addMana(player2, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.castAndResolveInstant(player1, 0, opposingCounter.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.assertInGraveyard(player2, "Access Denied");
        assertThat(gd.stack).noneMatch(entry -> entry.getTargetableId().equals(opposingCounter.getId()));
        harness.handleMayAbilityChosen(player1, false);
        assertThat(thopters(player1)).hasSize(5);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Creates no tokens when the targeted spell has already been countered")
    void missingTargetPreventsTokenCreation() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears, new AccessDenied()));
        harness.setHand(player2, List.of(new AccessDenied()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.addMana(player2, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.castAndResolveInstant(player1, 0, bears.getId());

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(thopters(player1)).hasSize(2);
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Access Denied");
        assertThat(thopters(player2)).isEmpty();
    }

    @Test
    @DisplayName("A zero-mana-value spell is countered without creating tokens")
    void zeroManaValueCreatesNoTokens() {
        Ornithopter ornithopter = new Ornithopter();
        harness.setHand(player1, List.of(ornithopter));
        harness.setHand(player2, List.of(new AccessDenied()));
        harness.addMana(player2, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, ornithopter.getId());

        harness.assertInGraveyard(player1, "Ornithopter");
        assertThat(thopters(player2)).isEmpty();
    }

    @Test
    @DisplayName("The chosen X contributes to the targeted spell's mana value")
    void countsChosenXOnStack() {
        Hurricane hurricane = new Hurricane();
        harness.setHand(player1, List.of(hurricane));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.setHand(player2, List.of(new AccessDenied()));
        harness.addMana(player2, ManaColor.BLUE, 5);

        harness.castSorcery(player1, 0, 3);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, hurricane.getId());

        harness.assertInGraveyard(player1, "Hurricane");
        assertThat(thopters(player2)).hasSize(4);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    private List<Permanent> thopters(com.github.laxika.magicalvibes.model.Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
    }
}
