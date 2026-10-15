package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.Burgeoning;
import com.github.laxika.magicalvibes.cards.r.RestInPeace;
import com.github.laxika.magicalvibes.cards.s.SkeletonScavengers;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MoggInfestation.class, MoggFlunkies.class, Burgeoning.class, RestInPeace.class, SkeletonScavengers.class})
class MoggInfestationTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target player's creatures and gives that player two Goblins per creature")
    void destroysTargetCreaturesAndCreatesGoblinTokensForTargetPlayer() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new MoggFlunkies());
        harness.addToBattlefield(player2, new MoggFlunkies());
        harness.addToBattlefield(player2, new MoggFlunkies());
        harness.addToBattlefield(player2, new Burgeoning());
        harness.setHand(player1, List.of(new MoggInfestation()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(countPermanents(player1, "Mogg Flunkies")).isEqualTo(1);
        assertThat(countPermanents(player1, "Goblin")).isZero();
        assertThat(countPermanents(player2, "Mogg Flunkies")).isZero();
        List<Permanent> goblins = findPermanents(player2, "Goblin");
        assertThat(goblins).hasSize(4);
        assertThat(goblins).allSatisfy(goblin -> {
            assertThat(goblin.getCard().isToken()).isTrue();
            assertThat(goblin.getCard().getPower()).isEqualTo(1);
            assertThat(goblin.getCard().getToughness()).isEqualTo(1);
            assertThat(goblin.getCard().getColor()).isEqualTo(CardColor.RED);
            assertThat(goblin.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(goblin.getCard().getSubtypes()).containsExactly(CardSubtype.GOBLIN);
        });
        harness.assertOnBattlefield(player2, "Burgeoning");
    }

    @Test
    @DisplayName("Can target the caster")
    void canTargetCaster() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new MoggFlunkies());
        harness.addToBattlefield(player2, new MoggFlunkies());
        harness.setHand(player1, List.of(new MoggInfestation()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        assertThat(countPermanents(player1, "Mogg Flunkies")).isZero();
        assertThat(countPermanents(player1, "Goblin")).isEqualTo(2);
        assertThat(countPermanents(player2, "Mogg Flunkies")).isEqualTo(1);
    }

    @Test
    @DisplayName("Creates no Goblins when the target player controls no creatures")
    void createsNoTokensWhenTargetHasNoCreatures() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player2, new Burgeoning());
        harness.setHand(player1, List.of(new MoggInfestation()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(countPermanents(player2, "Goblin")).isZero();
        harness.assertOnBattlefield(player2, "Burgeoning");
    }

    @Test
    @DisplayName("Cannot target a permanent")
    void cannotTargetPermanent() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player2, new MoggFlunkies());
        harness.setHand(player1, List.of(new MoggInfestation()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 2);

        var permanentId = findPermanent(player2, "Mogg Flunkies").getId();
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, permanentId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @CardUsed({RestInPeace.class})
    @DisplayName("Creatures exiled instead of dying do not produce Goblins")
    void exileReplacementCreatesNoTokens() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new RestInPeace());
        harness.addToBattlefield(player2, new MoggFlunkies());
        harness.setHand(player1, List.of(new MoggInfestation()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertNotOnBattlefield(player2, "Mogg Flunkies");
        harness.assertNotInGraveyard(player2, "Mogg Flunkies");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Mogg Flunkies"));
        assertThat(countPermanents(player2, "Goblin")).isZero();
    }

    @Test
    @CardUsed({SkeletonScavengers.class})
    @DisplayName("Regenerated creatures survive and do not produce Goblins")
    void regeneratedCreaturesDoNotCount() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new SkeletonScavengers(), "{2}{B}");
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.addToBattlefield(player1, new MoggFlunkies());
        harness.setHand(player1, List.of(new MoggInfestation()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        harness.assertOnBattlefield(player1, "Skeleton Scavengers");
        assertThat(findPermanent(player1, "Skeleton Scavengers").isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Mogg Flunkies");
        assertThat(countPermanents(player1, "Goblin")).isEqualTo(2);
    }

    @Test
    @DisplayName("Creature tokens that die also produce two Goblins each")
    void dyingTokensCount() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player2, new MoggFlunkies());
        harness.setHand(player1, List.of(new MoggInfestation(), new MoggInfestation()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        assertThat(countPermanents(player2, "Goblin")).isEqualTo(2);
        var originalTokenIds = findPermanents(player2, "Goblin").stream()
                .map(Permanent::getId).toList();

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(findPermanents(player2, "Goblin")).hasSize(4)
                .noneMatch(token -> originalTokenIds.contains(token.getId()));
    }
}
