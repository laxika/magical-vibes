package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ScatheZombies;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VictimOfNightTest extends BaseCardTest {

    private static Card createCreatureWithSubtype(String name, int power, int toughness,
                                                  CardColor color, CardSubtype subtype) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setColor(color);
        card.setPower(power);
        card.setToughness(toughness);
        card.setSubtypes(List.of(subtype));
        return card;
    }

    @Test
    @DisplayName("Victim of Night has correct card properties")
    void hasCorrectProperties() {
        VictimOfNight card = new VictimOfNight();

        assertThat(EffectResolution.needsTarget(card)).isTrue();
        assertThat(card.getTargetFilter()).isEqualTo(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentNotPredicate(new PermanentHasAnySubtypePredicate(
                                Set.of(CardSubtype.VAMPIRE, CardSubtype.WEREWOLF, CardSubtype.ZOMBIE)))
                )),
                "Target must be a non-Vampire, non-Werewolf, non-Zombie creature"
        ));
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst()).isInstanceOf(DestroyTargetPermanentEffect.class);
        DestroyTargetPermanentEffect effect = (DestroyTargetPermanentEffect) card.getEffects(EffectSlot.SPELL).getFirst();
        assertThat(effect.cannotBeRegenerated()).isFalse();
    }

    @Test
    @DisplayName("Casting Victim of Night targeting a valid creature puts it on stack")
    void castingPutsOnStack() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(bears);

        harness.setHand(player1, List.of(new VictimOfNight()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, bears.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Victim of Night");
        assertThat(entry.getTargetId()).isEqualTo(bears.getId());
    }

    @Test
    @DisplayName("Cannot target a Vampire creature")
    void cannotTargetVampire() {
        gd.playerBattlefields.get(player1.getId()).add(new Permanent(new GrizzlyBears()));

        Card vampire = createCreatureWithSubtype("Vampire Interloper", 2, 1, CardColor.BLACK, CardSubtype.VAMPIRE);
        Permanent vampirePerm = new Permanent(vampire);
        gd.playerBattlefields.get(player2.getId()).add(vampirePerm);

        harness.setHand(player1, List.of(new VictimOfNight()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, vampirePerm.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("non-Vampire");
    }

    @Test
    @DisplayName("Cannot target a Werewolf creature")
    void cannotTargetWerewolf() {
        gd.playerBattlefields.get(player1.getId()).add(new Permanent(new GrizzlyBears()));

        Card werewolf = createCreatureWithSubtype("Reckless Waif", 3, 2, CardColor.RED, CardSubtype.WEREWOLF);
        Permanent werewolfPerm = new Permanent(werewolf);
        gd.playerBattlefields.get(player2.getId()).add(werewolfPerm);

        harness.setHand(player1, List.of(new VictimOfNight()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, werewolfPerm.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("non-Werewolf");
    }

    @Test
    @DisplayName("Cannot target a Zombie creature")
    void cannotTargetZombie() {
        gd.playerBattlefields.get(player1.getId()).add(new Permanent(new GrizzlyBears()));

        Permanent zombie = new Permanent(new ScatheZombies());
        gd.playerBattlefields.get(player2.getId()).add(zombie);

        harness.setHand(player1, List.of(new VictimOfNight()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, zombie.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("non-Zombie");
    }

    @Test
    @DisplayName("Resolving Victim of Night destroys target creature and moves it to graveyard")
    void resolvingDestroysTargetCreature() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(bears);

        harness.setHand(player1, List.of(new VictimOfNight()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Victim of Night"));
    }

    @Test
    @DisplayName("Victim of Night allows regeneration")
    void allowsRegeneration() {
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setRegenerationShield(1);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        harness.setHand(player1, List.of(new VictimOfNight()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Victim of Night fizzles if target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(bears);

        harness.setHand(player1, List.of(new VictimOfNight()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, bears.getId());
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Victim of Night"));
    }
}
