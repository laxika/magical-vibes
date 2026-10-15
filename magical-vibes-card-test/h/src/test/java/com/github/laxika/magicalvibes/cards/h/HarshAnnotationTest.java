package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.e.EternalStudent;
import com.github.laxika.magicalvibes.cards.m.MasterfulFlourish;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HarshAnnotation.class, EternalStudent.class, Forest.class, MasterfulFlourish.class})
class HarshAnnotationTest extends BaseCardTest {

    

    @Test
    @DisplayName("Destroys target creature and gives its controller a 1/1 Inkling with flying")
    void destroysCreatureAndCreatesInklingForController() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new EternalStudent()).getId();

        harness.setHand(player1, List.of(new HarshAnnotation()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Eternal Student");
        harness.assertInGraveyard(player2, "Eternal Student");

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Inkling")
                        && p.getCard().isToken()
                        && p.getCard().hasType(CardType.CREATURE)
                        && p.getCard().getPower() == 1
                        && p.getCard().getToughness() == 1
                        && p.getCard().getSubtypes().contains(CardSubtype.INKLING)
                        && p.getCard().getKeywords().contains(Keyword.FLYING));
    }

    @Test
    @DisplayName("The Inkling token enters both white and black")
    void inklingTokenKeepsBothColors() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new EternalStudent()).getId();

        harness.setHand(player1, List.of(new HarshAnnotation()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, targetId);

        assertThat(findPermanent(player2, "Inkling")
                .getCard()
                .getColors())
                .containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLACK);
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetLand() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new HarshAnnotation()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID landId = harness.getPermanentId(player2, "Forest");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, landId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Fizzles if target is removed before resolution — no Inkling created")
    void fizzlesIfTargetRemoved() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new EternalStudent()).getId();

        harness.setHand(player1, List.of(new HarshAnnotation()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, targetId);
        gd.playerBattlefields.get(player2.getId()).removeIf(p -> p.getId().equals(targetId));
        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        harness.assertNotOnBattlefield(player2, "Inkling");
    }

    @Test
    @DisplayName("Can destroy your own creature and gives you exactly one Inkling")
    void destroysOwnCreatureAndCreatesInkling() {
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new EternalStudent()).getId();
        harness.setHand(player1, List.of(new HarshAnnotation()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player1, "Eternal Student");
        harness.assertInGraveyard(player1, "Eternal Student");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Inkling"))
                .hasSize(1);
        harness.assertNotOnBattlefield(player2, "Inkling");
    }

    @Test
    @DisplayName("An indestructible creature survives but its controller still gets an Inkling")
    void createsInklingEvenIfDestructionIsPrevented() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new EternalStudent()).getId();
        harness.setHand(player2, List.of(new MasterfulFlourish()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.castAndResolveInstant(player2, 0, targetId);

        harness.setHand(player1, List.of(new HarshAnnotation()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertOnBattlefield(player2, "Eternal Student");
        harness.assertNotInGraveyard(player2, "Eternal Student");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Inkling"))
                .hasSize(1);
        harness.assertNotOnBattlefield(player1, "Inkling");
    }

    @Test
    @DisplayName("The controller at resolution gets the Inkling after control changes")
    void usesControllerAtResolution() {
        var target = harness.addToBattlefieldAndReturn(player2, new EternalStudent());
        harness.setHand(player1, List.of(new HarshAnnotation()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, target.getId());

        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerBattlefields.get(player1.getId()).add(target);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Eternal Student");
        harness.assertNotOnBattlefield(player2, "Eternal Student");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Inkling"))
                .hasSize(1);
        harness.assertNotOnBattlefield(player2, "Inkling");
    }
}
