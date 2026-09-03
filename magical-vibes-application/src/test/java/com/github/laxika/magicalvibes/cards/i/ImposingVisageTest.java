package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ImposingVisage.class, GrizzlyBears.class, FountainOfYouth.class})
class ImposingVisageTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Imposing Visage attaches it to target creature")
    void resolvingAttachesToTarget() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new ImposingVisage()));
        harness.addMana(player1, ManaColor.RED, 1);

        gs.playCard(gd, player1, 0, 0, bearsPerm.getId(), null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Imposing Visage")
                        && p.isAttached()
                        && p.getAttachedTo().equals(bearsPerm.getId()));
    }

    @Test
    @DisplayName("Enchanted creature has menace")
    void enchantedCreatureHasMenace() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        Permanent visagePerm = new Permanent(new ImposingVisage());
        visagePerm.setAttachedTo(bearsPerm.getId());
        gd.playerBattlefields.get(player1.getId()).add(visagePerm);

        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("Creature loses menace when Imposing Visage is removed")
    void menaceStopsWhenRemoved() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        Permanent visagePerm = new Permanent(new ImposingVisage());
        visagePerm.setAttachedTo(bearsPerm.getId());
        gd.playerBattlefields.get(player1.getId()).add(visagePerm);

        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.MENACE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(visagePerm);

        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Imposing Visage does not affect other creatures")
    void doesNotAffectOtherCreatures() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        Permanent otherBears = addCreatureReady(player1, new GrizzlyBears());

        Permanent visagePerm = new Permanent(new ImposingVisage());
        visagePerm.setAttachedTo(bearsPerm.getId());
        gd.playerBattlefields.get(player1.getId()).add(visagePerm);

        assertThat(gqs.hasKeyword(gd, otherBears, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Imposing Visage can enchant an opponent's creature")
    void canEnchantOpponentCreature() {
        Permanent opponentBears = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ImposingVisage()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castEnchantment(player1, 0, opponentBears.getId());
        harness.passBothPriorities();

        Permanent visage = findPermanent(player1, "Imposing Visage");
        assertThat(visage.getAttachedTo()).isEqualTo(opponentBears.getId());
        assertThat(gqs.hasKeyword(gd, opponentBears, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("Menace from Imposing Visage prevents a single blocker")
    void menacePreventsSingleBlocker() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent visage = new Permanent(new ImposingVisage());
        visage.setAttachedTo(attacker.getId());
        gd.playerBattlefields.get(player1.getId()).add(visage);
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more creatures");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Imposing Visage")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new ImposingVisage()));
        harness.addMana(player1, ManaColor.RED, 1);

        Permanent artifact = findPermanent(player1, "Fountain of Youth");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
