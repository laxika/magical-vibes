package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AngelsMercy;
import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MirriCatWarrior;
import com.github.laxika.magicalvibes.cards.q.QuandrixPledgemage;
import com.github.laxika.magicalvibes.cards.r.RecklessAmplimancer;
import com.github.laxika.magicalvibes.cards.v.VanishingVerse;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DoubleMajor.class, AngelsMercy.class, Cancel.class, GrizzlyBears.class,
        MirriCatWarrior.class, QuandrixPledgemage.class, RecklessAmplimancer.class, VanishingVerse.class})
class DoubleMajorTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a token copy of a creature spell you control")
    void createsTokenCopyOfCreatureSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears, new DoubleMajor()));
        addManaForDoubleMajor();

        harness.castCreature(player1, 0);
        harness.castAndResolveInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(1);
        assertThat(tokens.getFirst().getCard().getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("A legendary creature spell becomes a nonlegendary token copy")
    void legendaryCreatureSpellBecomesNonlegendaryTokenCopy() {
        MirriCatWarrior mirri = new MirriCatWarrior();
        harness.setHand(player1, List.of(mirri, new DoubleMajor()));
        addManaForDoubleMajor();

        harness.castCreature(player1, 0);
        harness.castAndResolveInstant(player1, 0, mirri.getId());
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().getSupertypes()).doesNotContain(CardSupertype.LEGENDARY);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Cannot target an instant spell")
    void cannotTargetInstantSpell() {
        AngelsMercy mercy = new AngelsMercy();
        harness.setHand(player1, List.of(mercy, new DoubleMajor()));
        addManaForDoubleMajor();

        harness.castInstant(player1, 0);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, mercy.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void tokenCopyRetainsActivatedAbilities() {
        RecklessAmplimancer amplimancer = new RecklessAmplimancer();
        harness.setHand(player1, List.of(amplimancer, new DoubleMajor()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.castAndResolveInstant(player1, 0, amplimancer.getId());
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(token.getCard().isToken()).isTrue();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(token.getEffectivePower()).isEqualTo(4);
        assertThat(token.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    void cannotTargetOpponentsCreatureSpell() {
        RecklessAmplimancer amplimancer = new RecklessAmplimancer();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(amplimancer));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.setHand(player1, List.of(new DoubleMajor()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player2, 0);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, amplimancer.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetCreatureAlreadyOnBattlefield() {
        RecklessAmplimancer amplimancer = new RecklessAmplimancer();
        harness.addToBattlefield(player1, amplimancer);
        harness.setHand(player1, List.of(new DoubleMajor()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, amplimancer.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void createsNoCopyWhenOriginalIsCounteredBeforeResolution() {
        RecklessAmplimancer amplimancer = new RecklessAmplimancer();
        harness.setHand(player1, List.of(amplimancer, new DoubleMajor()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.castInstant(player1, 0, amplimancer.getId());
        harness.castAndResolveInstant(player2, 0, amplimancer.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Reckless Amplimancer");
        harness.assertInGraveyard(player1, "Double Major");
    }

    @Test
    void copyResolvesIndependentlyOfCounteredOriginal() {
        RecklessAmplimancer amplimancer = new RecklessAmplimancer();
        harness.setHand(player1, List.of(amplimancer, new DoubleMajor()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.castAndResolveInstant(player1, 0, amplimancer.getId());
        harness.castAndResolveInstant(player2, 0, amplimancer.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().getCard().isToken()).isTrue();
        harness.assertInGraveyard(player1, "Reckless Amplimancer");
    }


    @Test
    void multicoloredCopyCannotBeTargetedByVanishingVerse() {
        QuandrixPledgemage pledgemage = new QuandrixPledgemage();
        harness.setHand(player1, List.of(pledgemage, new DoubleMajor()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.setHand(player2, List.of(new VanishingVerse()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        harness.castAndResolveInstant(player1, 0, pledgemage.getId());
        harness.passBothPriorities();
        Permanent token = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(token.getCard().isToken()).isTrue();

        assertThatThrownBy(() -> harness.castInstant(player2, 0, token.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void originalLegendaryCreatureAndNonlegendaryCopyBothRemain() {
        MirriCatWarrior mirri = new MirriCatWarrior();
        harness.setHand(player1, List.of(mirri, new DoubleMajor()));
        addManaForDoubleMajor();

        harness.castCreature(player1, 0);
        harness.castAndResolveInstant(player1, 0, mirri.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anySatisfy(permanent -> {
                    assertThat(permanent.getCard().isToken()).isFalse();
                    assertThat(permanent.getCard().getSupertypes()).contains(CardSupertype.LEGENDARY);
                })
                .anySatisfy(permanent -> {
                    assertThat(permanent.getCard().isToken()).isTrue();
                    assertThat(permanent.getCard().getSupertypes()).doesNotContain(CardSupertype.LEGENDARY);
                });
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void addManaForDoubleMajor() {
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }
}
