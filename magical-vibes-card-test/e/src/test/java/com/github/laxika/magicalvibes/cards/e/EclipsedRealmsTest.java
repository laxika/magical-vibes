package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.b.BoggartPrankster;
import com.github.laxika.magicalvibes.cards.f.FormidableSpeaker;
import com.github.laxika.magicalvibes.cards.g.GhostlyChangeling;
import com.github.laxika.magicalvibes.cards.n.NamelessInversion;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EclipsedRealms.class, BoggartPrankster.class, FormidableSpeaker.class, NamelessInversion.class})
class EclipsedRealmsTest extends BaseCardTest {

    @Test
    @DisplayName("Choosing a subtype offers only the eight types named by Eclipsed Realms")
    void subtypeChoiceIsRestricted() {
        harness.setHand(player1, List.of(new EclipsedRealms()));

        harness.playLand(player1, 0);

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactly(
                "ELEMENTAL", "ELF", "FAERIE", "GIANT", "GOBLIN", "KITHKIN", "MERFOLK", "TREEFOLK");
    }

    @Test
    @DisplayName("The first ability adds one colorless mana")
    void firstAbilityAddsColorlessMana() {
        Permanent land = addLand(CardSubtype.ELF);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("The second ability adds mana restricted to the chosen type's spells and abilities")
    void secondAbilityAddsChosenTypeRestrictedMana() {
        addLand(CardSubtype.FAERIE);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "BLUE");

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.BLUE)).isZero();
        assertThat(pool.getSubtypeSpellOrAbilityManaForColor(Set.of(CardSubtype.FAERIE), ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Restricted mana can cast a spell of the chosen type")
    void restrictedManaCanCastChosenTypeSpell() {
        addLandAndProduceMana(CardSubtype.GOBLIN, ManaColor.BLACK);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new BoggartPrankster()));

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Restricted mana cannot cast a spell of another type")
    void restrictedManaCannotCastAnotherTypeSpell() {
        addLandAndProduceMana(CardSubtype.FAERIE, ManaColor.BLACK);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new BoggartPrankster()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Restricted mana can activate an ability of a source of the chosen type")
    void restrictedManaCanActivateChosenTypeAbility() {
        addLandAndProduceMana(CardSubtype.ELEMENTAL, ManaColor.RED);
        harness.addToBattlefield(player1,
                createCreatureWithLifeAbility("Ability Elemental", CardSubtype.ELEMENTAL));

        int lifeBefore = gd.getLife(player1.getId());
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 3);
    }

    @Test
    @DisplayName("Restricted mana cannot activate an ability of a source of another type")
    void restrictedManaCannotActivateAnotherTypeAbility() {
        addLandAndProduceMana(CardSubtype.ELEMENTAL, ManaColor.RED);
        harness.addToBattlefield(player1, createCreatureWithLifeAbility("Ability Elf", CardSubtype.ELF));

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The chosen type is stored during entry without using the stack")
    void choiceCompletesDuringEntry() {
        harness.setHand(player1, List.of(new EclipsedRealms()));
        harness.playLand(player1, 0);

        assertThat(gd.stack).isEmpty();
        harness.handleListChoice(player1, "TREEFOLK");

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().getChosenSubtype())
                .isEqualTo(CardSubtype.TREEFOLK);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "GREEN");
        assertThat(gd.playerManaPools.get(player1.getId())
                .getSubtypeSpellOrAbilityManaForColor(Set.of(CardSubtype.TREEFOLK), ManaColor.GREEN))
                .isEqualTo(1);
    }

    @Test
    @DisplayName("A creature type not named on the land cannot be chosen")
    void invalidSubtypeChoiceIsRejected() {
        harness.setHand(player1, List.of(new EclipsedRealms()));
        harness.playLand(player1, 0);

        assertThatThrownBy(() -> harness.handleListChoice(player1, "HUMAN"))
                .isInstanceOf(IllegalArgumentException.class);
        harness.handleListChoice(player1, "ELF");
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().getChosenSubtype())
                .isEqualTo(CardSubtype.ELF);
    }

    @Test
    @DisplayName("Colorless mana can pay for a spell of a different creature type")
    void colorlessManaIsUnrestricted() {
        addLand(CardSubtype.FAERIE);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setHand(player1, List.of(new BoggartPrankster()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Boggart Prankster")).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Restricted colored mana can pay a matching source's generic activation cost")
    void restrictedManaPaysGenericActivationCost() {
        addLandAndProduceMana(CardSubtype.ELF, ManaColor.BLUE);
        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();
        addCreatureReady(player1, new FormidableSpeaker());

        harness.activateAbility(player1, 1, null, land.getId());
        harness.passBothPriorities();

        assertThat(land.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId())
                .getSubtypeSpellOrAbilityManaForColor(Set.of(CardSubtype.ELF), ManaColor.BLUE)).isZero();
    }

    @Test
    @DisplayName("Restricted mana cannot pay a generic activation cost for a nonmatching source")
    void restrictedManaCannotPayNonmatchingGenericActivationCost() {
        addLandAndProduceMana(CardSubtype.FAERIE, ManaColor.BLUE);
        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();
        addCreatureReady(player1, new FormidableSpeaker());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(land.isTapped()).isTrue();
    }

    @Test
    @CardUsed({GhostlyChangeling.class})
    @DisplayName("Restricted mana can cast a changeling regardless of the chosen type")
    void restrictedManaCastsChangeling() {
        addLandAndProduceMana(CardSubtype.ELF, ManaColor.BLACK);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setHand(player1, List.of(new GhostlyChangeling()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Ghostly Changeling")).isEqualTo(1);
    }

    @Test
    @CardUsed({GhostlyChangeling.class})
    @DisplayName("Restricted mana can activate a changeling's ability for any chosen type")
    void restrictedManaActivatesChangeling() {
        addLandAndProduceMana(CardSubtype.ELF, ManaColor.BLACK);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        Permanent changeling = harness.addToBattlefieldAndReturn(player1, new GhostlyChangeling());
        int powerBefore = gqs.getEffectivePower(gd, changeling);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, changeling)).isEqualTo(powerBefore + 1);
        assertThat(gd.playerManaPools.get(player1.getId())
                .getSubtypeSpellOrAbilityManaForColor(Set.of(CardSubtype.ELF), ManaColor.BLACK)).isZero();
    }

    @Test
    @DisplayName("Restricted mana can cast a matching kindred instant")
    void restrictedManaCastsKindredInstant() {
        addLandAndProduceMana(CardSubtype.TREEFOLK, ManaColor.BLACK);
        Permanent speaker = addCreatureReady(player1, new FormidableSpeaker());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new NamelessInversion()));
        int powerBefore = gqs.getEffectivePower(gd, speaker);

        harness.castInstant(player1, 0, speaker.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, speaker)).isEqualTo(powerBefore + 3);
        assertThat(gd.playerManaPools.get(player1.getId())
                .getSubtypeSpellOrAbilityManaForColor(Set.of(CardSubtype.TREEFOLK), ManaColor.BLACK)).isZero();
    }

    @Test
    @DisplayName("Restricted mana cannot activate a source that has lost the chosen creature type")
    void restrictedManaCannotPayAfterSourceLosesChosenType() {
        Permanent land = addLand(CardSubtype.ELF);
        Permanent speaker = addCreatureReady(player1, new FormidableSpeaker());
        harness.setHand(player1, List.of(new NamelessInversion()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, speaker.getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @CardUsed({ElvishAberration.class})
    @DisplayName("Restricted mana can pay for an ability of a matching card in hand")
    void restrictedManaPaysMatchingHandAbility() {
        addLandAndProduceMana(CardSubtype.ELF, ManaColor.RED);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        ElvishAberration elf = new ElvishAberration();
        harness.setHand(player1, List.of(elf));

        harness.activateHandAbility(player1, 0, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(elf);
        assertThat(gd.playerManaPools.get(player1.getId())
                .getSubtypeSpellOrAbilityManaForColor(Set.of(CardSubtype.ELF), ManaColor.RED)).isZero();
    }

    private Permanent addLand(CardSubtype chosenSubtype) {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new EclipsedRealms());
        land.setChosenSubtype(chosenSubtype);
        return land;
    }

    private void addLandAndProduceMana(CardSubtype chosenSubtype, ManaColor color) {
        addLand(chosenSubtype);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, color.name());
    }

    private static Card createCreatureWithLifeAbility(String name, CardSubtype subtype) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{2}");
        card.setColor(CardColor.RED);
        card.setPower(2);
        card.setToughness(2);
        card.setSubtypes(List.of(subtype));
        card.addActivatedAbility(new ActivatedAbility(
                false, "{R}", List.of(new GainLifeEffect(3)), "{R}: You gain 3 life."));
        return card;
    }
}
