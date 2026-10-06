package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AccessTunnel;
import com.github.laxika.magicalvibes.cards.e.EagerFirstYear;
import com.github.laxika.magicalvibes.cards.l.LetterOfAcceptance;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ReduceToMemory.class, EagerFirstYear.class, AccessTunnel.class, LetterOfAcceptance.class})
class ReduceToMemoryTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a target nonland permanent and its controller creates a 3/2 Spirit")
    void exilesTargetNonlandPermanentAndItsControllerCreatesSpirit() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new EagerFirstYear()).getId();

        castReduceToMemory(targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Eager First-Year");
        harness.assertNotInGraveyard(player2, "Eager First-Year");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Eager First-Year"));

        List<Permanent> spirits = findPermanents(player2, "Spirit").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(spirits).singleElement().satisfies(spirit -> {
            assertThat(spirit.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(spirit.getCard().getPower()).isEqualTo(3);
            assertThat(spirit.getCard().getToughness()).isEqualTo(2);
            assertThat(spirit.getCard().getColor()).isEqualTo(CardColor.RED);
            assertThat(spirit.getCard().getColors())
                    .containsExactlyInAnyOrder(CardColor.RED, CardColor.WHITE);
            assertThat(spirit.getCard().getSubtypes()).containsExactly(CardSubtype.SPIRIT);
        });
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Spirit"));
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new AccessTunnel()).getId();

        harness.setHand(player1, List.of(new ReduceToMemory()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonland permanent");
    }

    @Test
    @DisplayName("Does not create a Spirit when the target leaves before resolution")
    void fizzlesWhenTargetLeavesBeforeResolution() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new EagerFirstYear()).getId();

        castReduceToMemory(targetId);
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Spirit"));
    }

    @Test
    @DisplayName("Can exile your own noncreature permanent and give you the Spirit")
    void exilesOwnArtifactAndCreatesSpiritForCaster() {
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new LetterOfAcceptance()).getId();

        castReduceToMemory(targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Letter of Acceptance");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Letter of Acceptance"));
        assertThat(findPermanents(player1, "Spirit")).singleElement()
                .satisfies(spirit -> assertThat(spirit.getCard().isToken()).isTrue());
        assertThat(countPermanents(player2, "Spirit")).isZero();
    }

    @Test
    @DisplayName("Uses the target's controller at resolution after control changes")
    void createsSpiritForControllerAtResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new EagerFirstYear());
        castReduceToMemory(target.getId());

        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerBattlefields.get(player1.getId()).add(target);
        gd.stolenCreatures.put(target.getId(), player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Eager First-Year"));
        assertThat(countPermanents(player1, "Spirit")).isEqualTo(1);
        assertThat(countPermanents(player2, "Spirit")).isZero();
    }

    @Test
    @DisplayName("Exiling a Spirit token still creates a replacement Spirit")
    void exilesTokenAndCreatesReplacement() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new EagerFirstYear()).getId();
        castReduceToMemory(targetId);
        harness.passBothPriorities();
        UUID originalSpiritId = findPermanent(player2, "Spirit").getId();

        castReduceToMemory(originalSpiritId);
        harness.passBothPriorities();

        assertThat(findPermanents(player2, "Spirit")).singleElement().satisfies(spirit -> {
            assertThat(spirit.getId()).isNotEqualTo(originalSpiritId);
            assertThat(spirit.getCard().isToken()).isTrue();
        });
        assertThat(countPermanents(player1, "Spirit")).isZero();
    }

    private void castReduceToMemory(UUID targetId) {
        harness.setHand(player1, List.of(new ReduceToMemory()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castSorcery(player1, 0, targetId);
    }
}
