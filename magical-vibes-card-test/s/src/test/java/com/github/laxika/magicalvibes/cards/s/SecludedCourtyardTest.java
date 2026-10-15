package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.r.ReassemblingSkeleton;
import com.github.laxika.magicalvibes.cards.v.VampireNeonate;
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

@CardUsed({SecludedCourtyard.class, LlanowarElves.class, ShivanDragon.class,
        VampireNeonate.class, ReassemblingSkeleton.class, SoulstoneSanctuary.class})
class SecludedCourtyardTest extends BaseCardTest {

    @Test
    @DisplayName("Choosing a creature type when Secluded Courtyard enters stores that type")
    void choosingCreatureTypeWhenEntering() {
        harness.setHand(player1, List.of(new SecludedCourtyard()));

        harness.playLand(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "VAMPIRE");

        assertThat(findPermanent(player1, "Secluded Courtyard").getChosenSubtype())
                .isEqualTo(CardSubtype.VAMPIRE);
    }

    @Test
    @DisplayName("The first ability adds one colorless mana")
    void firstAbilityAddsColorlessMana() {
        Permanent courtyard = addCourtyard(CardSubtype.VAMPIRE);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(courtyard.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("The second ability adds mana restricted to creature spells and creature-source abilities")
    void secondAbilityAddsRestrictedMana() {
        addCourtyard(CardSubtype.MERFOLK);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "BLUE");

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.BLUE)).isZero();
        assertThat(pool.getSubtypeCreatureSourceSpellOrAbilityManaForColor(
                Set.of(CardSubtype.MERFOLK), ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Restricted mana can cast a creature spell of the chosen type")
    void restrictedManaCanCastChosenCreatureSpell() {
        addCourtyardAndProduceMana(CardSubtype.VAMPIRE, ManaColor.RED);

        Card vampire = createCard("Test Vampire", CardType.CREATURE, "{R}", CardColor.RED,
                CardSubtype.VAMPIRE);
        harness.setHand(player1, List.of(vampire));

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Restricted mana cannot cast a noncreature spell even if it has the chosen subtype")
    void restrictedManaCannotCastNoncreatureSpell() {
        addCourtyardAndProduceMana(CardSubtype.VAMPIRE, ManaColor.RED);

        Card spell = createCard("Test Vampire Spell", CardType.INSTANT, "{R}", CardColor.RED,
                CardSubtype.VAMPIRE);
        harness.setHand(player1, List.of(spell));

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Restricted mana cannot cast a creature spell of a different type")
    void restrictedManaCannotCastDifferentCreatureType() {
        addCourtyardAndProduceMana(CardSubtype.VAMPIRE, ManaColor.GREEN);

        Card elf = createCard("Test Elf", CardType.CREATURE, "{G}", CardColor.GREEN, CardSubtype.ELF);
        harness.setHand(player1, List.of(elf));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Restricted mana can activate an ability of a creature source of the chosen type")
    void restrictedManaCanActivateChosenCreatureSourceAbility() {
        addCourtyardAndProduceMana(CardSubtype.ELEMENTAL, ManaColor.RED);
        harness.addToBattlefield(player1,
                createAbilitySource("Ability Elemental", CardType.CREATURE, CardSubtype.ELEMENTAL));

        int lifeBefore = gd.getLife(player1.getId());
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 3);
    }

    @Test
    @DisplayName("Restricted mana cannot activate an ability of a noncreature source")
    void restrictedManaCannotActivateNoncreatureSourceAbility() {
        addCourtyardAndProduceMana(CardSubtype.ELEMENTAL, ManaColor.RED);
        harness.addToBattlefield(player1,
                createAbilitySource("Noncreature Elemental", CardType.ARTIFACT, CardSubtype.ELEMENTAL));

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Restricted mana pays the full cost of Llanowar Elves when Elf is chosen")
    void restrictedManaCastsRealChosenCreature() {
        addCourtyardAndProduceMana(CardSubtype.ELF, ManaColor.GREEN);
        harness.setHand(player1, List.of(new LlanowarElves()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Llanowar Elves");
        assertThat(gd.playerManaPools.get(player1.getId())
                .getSubtypeCreatureSourceSpellOrAbilityManaTotal(Set.of(CardSubtype.ELF))).isZero();
    }

    @Test
    @DisplayName("Restricted colored mana can pay generic costs of a matching creature ability")
    void restrictedManaPaysGenericCreatureAbilityCost() {
        addCourtyardAndProduceMana(CardSubtype.VAMPIRE, ManaColor.BLUE);
        Permanent neonate = harness.addToBattlefieldAndReturn(player1, new VampireNeonate());
        neonate.setSummoningSick(false);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
        assertThat(neonate.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Restricted mana cannot pay for an ability of a different creature type")
    void restrictedManaCannotActivateDifferentRealCreatureType() {
        addCourtyardAndProduceMana(CardSubtype.VAMPIRE, ManaColor.RED);
        harness.addToBattlefield(player1, new ShivanDragon());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Restricted mana can pay a matching creature card's graveyard ability")
    void restrictedManaPaysCreatureSourceAbilityInGraveyard() {
        addCourtyardAndProduceMana(CardSubtype.SKELETON, ManaColor.BLACK);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setGraveyard(player1, List.of(new ReassemblingSkeleton()));

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Reassembling Skeleton");
        harness.assertNotInGraveyard(player1, "Reassembling Skeleton");
        assertThat(findPermanent(player1, "Reassembling Skeleton").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Restricted mana can pay an ability of an animated creature with all creature types")
    void restrictedManaPaysAbilityOfCreatureWithAllTypes() {
        addCourtyard(CardSubtype.VAMPIRE);
        harness.addToBattlefield(player1, new SoulstoneSanctuary());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "BLUE");
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId())
                .getSubtypeCreatureSourceSpellOrAbilityManaTotal(Set.of(CardSubtype.VAMPIRE))).isZero();
    }

    @Test
    @DisplayName("Restricted mana cannot animate Soulstone Sanctuary before it is a creature")
    void restrictedManaCannotPayNoncreatureLandAbility() {
        addCourtyardAndProduceMana(CardSubtype.VAMPIRE, ManaColor.BLUE);
        harness.addToBattlefield(player1, new SoulstoneSanctuary());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addCourtyard(CardSubtype chosenSubtype) {
        Permanent courtyard = harness.addToBattlefieldAndReturn(player1, new SecludedCourtyard());
        courtyard.setChosenSubtype(chosenSubtype);
        return courtyard;
    }

    private void addCourtyardAndProduceMana(CardSubtype chosenSubtype, ManaColor color) {
        addCourtyard(chosenSubtype);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, color.name());
    }

    private static Card createCard(String name, CardType type, String manaCost, CardColor color,
                                   CardSubtype subtype) {
        Card card = new Card();
        card.setName(name);
        card.setType(type);
        card.setManaCost(manaCost);
        card.setColor(color);
        card.setSubtypes(List.of(subtype));
        if (type == CardType.CREATURE) {
            card.setPower(2);
            card.setToughness(2);
        }
        return card;
    }

    private static Card createAbilitySource(String name, CardType type, CardSubtype subtype) {
        Card card = createCard(name, type, "{2}", CardColor.RED, subtype);
        card.addActivatedAbility(new ActivatedAbility(
                false, "{R}", List.of(new GainLifeEffect(3)), "{R}: You gain 3 life."));
        return card;
    }
}
