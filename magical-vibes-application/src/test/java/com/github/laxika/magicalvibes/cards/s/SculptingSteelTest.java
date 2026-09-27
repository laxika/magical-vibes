package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AuriokBladewarden;
import com.github.laxika.magicalvibes.cards.b.BoshIronGolem;
import com.github.laxika.magicalvibes.cards.c.CobaltGolem;
import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.cards.w.WeldingJar;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SculptingSteel.class, IcyManipulator.class, AuriokBladewarden.class, CobaltGolem.class,
        WeldingJar.class, Shatter.class, BoshIronGolem.class})
class SculptingSteelTest extends BaseCardTest {

    @Test
    @DisplayName("Sculpting Steel copies a noncreature artifact and its activated ability")
    void copiesNonCreatureArtifactAndItsActivatedAbility() {
        Permanent manipulator = harness.addToBattlefieldAndReturn(player2, new IcyManipulator());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AuriokBladewarden());
        harness.setHand(player1, List.of(new SculptingSteel()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handlePermanentChosen(player1, manipulator.getId());

        Permanent steelPerm = findSculptingSteel();
        assertThat(steelPerm.getCard().getName()).isEqualTo("Icy Manipulator");
        assertThat(steelPerm.getCard().getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(steelPerm.getCard().getActivatedAbilities()).hasSize(1);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(steelPerm), null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Sculpting Steel copies an artifact creature's power, toughness, and abilities")
    void copiesArtifactCreature() {
        Permanent golem = harness.addToBattlefieldAndReturn(player2, new CobaltGolem());
        harness.setHand(player1, List.of(new SculptingSteel()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, golem.getId());

        Permanent steelPerm = findSculptingSteel();
        assertThat(steelPerm.getCard().getName()).isEqualTo("Cobalt Golem");
        assertThat(steelPerm.getCard().getPower()).isEqualTo(2);
        assertThat(steelPerm.getCard().getToughness()).isEqualTo(3);
        assertThat(steelPerm.getCard().getActivatedAbilities()).hasSize(1);
    }

    @Test
    @DisplayName("Sculpting Steel does not offer to copy non-artifact creatures")
    void doesNotOfferNonArtifactCreatures() {
        harness.addToBattlefield(player2, new AuriokBladewarden());
        harness.setHand(player1, List.of(new SculptingSteel()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        Permanent steelPerm = findSculptingSteel();
        assertThat(steelPerm.getCard().getName()).isEqualTo("Sculpting Steel");
    }

    @Test
    @DisplayName("Sculpting Steel enters as itself when no artifacts are on the battlefield")
    void entersAsItselfWhenNoArtifacts() {
        harness.setHand(player1, List.of(new SculptingSteel()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent steelPerm = findSculptingSteel();
        assertThat(steelPerm.getCard().getName()).isEqualTo("Sculpting Steel");
    }

    @Test
    @DisplayName("Sculpting Steel enters as itself when the player declines to copy")
    void entersAsItselfWhenPlayerDeclines() {
        harness.addToBattlefield(player2, new IcyManipulator());
        harness.setHand(player1, List.of(new SculptingSteel()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        Permanent steelPerm = findSculptingSteel();
        assertThat(steelPerm.getCard().getName()).isEqualTo("Sculpting Steel");
    }

    @Test
    @DisplayName("Sculpting Steel goes to the graveyard as Sculpting Steel when destroyed")
    void goesToGraveyardAsSculptingSteel() {
        Permanent jar = harness.addToBattlefieldAndReturn(player2, new WeldingJar());
        harness.setHand(player1, List.of(new SculptingSteel()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, jar.getId());

        Permanent steelPerm = findSculptingSteel();
        harness.setHand(player1, List.of(new Shatter()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, steelPerm.getId());

        harness.assertNotOnBattlefield(player1, "Welding Jar");
        harness.assertInGraveyard(player1, "Sculpting Steel");
        harness.assertNotInGraveyard(player1, "Welding Jar");
        harness.assertOnBattlefield(player2, "Welding Jar");
    }

    @Test
    @DisplayName("Sculpting Steel triggers the legend rule when copying a legendary artifact")
    void triggersLegendRule() {
        Permanent original = harness.addToBattlefieldAndReturn(player1, new BoshIronGolem());
        harness.setHand(player1, List.of(new SculptingSteel()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, original.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId())
                .isEqualTo(player1.getId());
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.LegendRule.class);
        assertThat(countPermanents(player1, "Bosh, Iron Golem")).isEqualTo(2);

        harness.handlePermanentChosen(player1, original.getId());

        assertThat(countPermanents(player1, "Bosh, Iron Golem")).isEqualTo(1);
        harness.assertInGraveyard(player1, "Sculpting Steel");
    }

    @Test
    @DisplayName("Sculpting Steel can copy an artifact controlled by its own controller")
    void copiesOwnArtifact() {
        Permanent manipulator = harness.addToBattlefieldAndReturn(player1, new IcyManipulator());
        harness.setHand(player1, List.of(new SculptingSteel()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, manipulator.getId());

        Permanent steelPerm = findSculptingSteel();
        assertThat(steelPerm.getCard().getName()).isEqualTo("Icy Manipulator");
    }

    private Permanent findSculptingSteel() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard() instanceof SculptingSteel)
                .findFirst()
                .orElseThrow();
    }
}
