package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.k.KamiOfOldStone;
import com.github.laxika.magicalvibes.cards.s.SenseisDiviningTop;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Vigilance.class, KamiOfOldStone.class, SenseisDiviningTop.class})
class VigilanceTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Vigilance attaches it to target creature")
    void resolvingAttachesToTarget() {
        Permanent creature = addCreatureReady(player1, new KamiOfOldStone());

        harness.setHand(player1, List.of(new Vigilance()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        gs.playCard(gd, player1, 0, 0, creature.getId(), null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Vigilance")
                        && p.isAttached()
                        && p.getAttachedTo().equals(creature.getId()));
    }

    @Test
    @DisplayName("Enchanted creature has vigilance")
    void enchantedCreatureHasVigilance() {
        Permanent creature = addCreatureReady(player1, new KamiOfOldStone());

        Permanent auraPerm = new Permanent(new Vigilance());
        auraPerm.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(auraPerm);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Creature loses vigilance when Vigilance is removed")
    void vigilanceStopsWhenRemoved() {
        Permanent creature = addCreatureReady(player1, new KamiOfOldStone());

        Permanent auraPerm = new Permanent(new Vigilance());
        auraPerm.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(auraPerm);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(auraPerm);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Vigilance")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new SenseisDiviningTop());
        // A legal creature target must exist somewhere, or the aura is unplayable before targeting
        // is ever validated (CR 601.2c).
        harness.addToBattlefield(player2, new KamiOfOldStone());
        harness.setHand(player1, List.of(new Vigilance()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Vigilance does not affect other creatures")
    void doesNotAffectOtherCreatures() {
        Permanent creature = addCreatureReady(player1, new KamiOfOldStone());

        Permanent otherCreature = addCreatureReady(player1, new KamiOfOldStone());

        Permanent auraPerm = new Permanent(new Vigilance());
        auraPerm.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(auraPerm);

        assertThat(gqs.hasKeyword(gd, otherCreature, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Vigilance can enchant an opponent's creature")
    void canEnchantOpponentsCreature() {
        Permanent creature = addCreatureReady(player2, new KamiOfOldStone());

        harness.setHand(player1, List.of(new Vigilance()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Vigilance");
        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Vigilance keeps the enchanted creature untapped when it attacks")
    void vigilanceKeepsEnchantedCreatureUntappedWhenAttacking() {
        Permanent creature = addCreatureReady(player1, new KamiOfOldStone());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Vigilance());
        aura.setAttachedTo(creature.getId());

        declareAttackers(List.of(0));

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Vigilance goes to its owner's graveyard if its target leaves before resolution")
    void fizzlesIfTargetLeavesBeforeResolution() {
        Permanent creature = addCreatureReady(player2, new KamiOfOldStone());

        harness.setHand(player1, List.of(new Vigilance()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castEnchantment(player1, 0, creature.getId());

        gd.playerBattlefields.get(player2.getId()).remove(creature);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Vigilance");
        harness.assertNotOnBattlefield(player1, "Vigilance");
    }
}
