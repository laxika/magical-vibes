package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AwakenedSkyclave;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GarrukWildspeaker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.InvasionOfZendikar;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
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

@CardUsed({VowOfMalice.class, GrizzlyBears.class, FountainOfYouth.class, GarrukWildspeaker.class, Ornithopter.class, RagingGoblin.class, InvasionOfZendikar.class, AwakenedSkyclave.class})
class VowOfMaliceTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Vow of Malice attaches it and grants +2/+2 and intimidate")
    void castsAndGrantsBoostAndIntimidate() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        VowOfMalice vow = new VowOfMalice();
        harness.setHand(player1, List.of(vow));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.INTIMIDATE)).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard() == vow && p.getAttachedTo().equals(creature.getId()));
    }

    @Test
    @DisplayName("Enchanted creature cannot attack the Aura controller")
    void enchantedCreatureCannotAttackAuraController() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = new Permanent(new VowOfMalice());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player2.getId()).add(aura);

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Vow of Malice effects end when the Aura leaves")
    void effectsEndWhenRemoved() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = new Permanent(new VowOfMalice());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.INTIMIDATE)).isFalse();
    }

    @Test
    @DisplayName("Vow of Malice cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new VowOfMalice()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("The enchanted creature cannot attack the Aura controller's planeswalker")
    void cannotAttackAuraControllersPlaneswalker() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new VowOfMalice());
        aura.setAttachedTo(creature.getId());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new GarrukWildspeaker());

        assertThat(als.canAttackDefender(gd, creature, planeswalker.getId())).isFalse();
    }

    @Test
    @DisplayName("A creature enchanted by its own controller can attack the opponent")
    void canAttackOpponentWhenAuraHasSameController() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new VowOfMalice());
        aura.setAttachedTo(creature.getId());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(creature.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Removing the Aura ends its attack restriction")
    void attackRestrictionEndsWhenAuraLeaves() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new VowOfMalice());
        aura.setAttachedTo(creature.getId());
        assertThat(als.canAttackDefender(gd, creature, player2.getId())).isFalse();

        gd.playerBattlefields.get(player2.getId()).remove(aura);

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(creature.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Intimidate uses the creature's colors and permits artifact blockers")
    void intimidateRestrictsBlocking() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new VowOfMalice());
        aura.setAttachedTo(creature.getId());
        Permanent greenBlocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent redBlocker = addCreatureReady(player2, new RagingGoblin());
        Permanent artifactBlocker = addCreatureReady(player2, new Ornithopter());

        declareAttackersAndPrepareBlockers(List.of(0));

        List<Permanent> defenders = gd.playerBattlefields.get(player2.getId());
        assertThat(bls.canBlockAttacker(gd, greenBlocker, creature, defenders)).isTrue();
        assertThat(bls.canBlockAttacker(gd, redBlocker, creature, defenders)).isFalse();
        assertThat(bls.canBlockAttacker(gd, artifactBlocker, creature, defenders)).isTrue();
    }

    @Test
    @DisplayName("The Aura does not prevent attacks against battles its controller controls")
    void canAttackBattleControlledByAuraController() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent battle = harness.addToBattlefieldAndReturn(player1, new InvasionOfZendikar());
        battle.setProtectorPlayerId(player2.getId());
        assertThat(als.canAttackDefender(gd, creature, battle.getId())).isTrue();

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new VowOfMalice());
        aura.setAttachedTo(creature.getId());

        assertThat(als.canAttackDefender(gd, creature, battle.getId())).isTrue();
    }
}
