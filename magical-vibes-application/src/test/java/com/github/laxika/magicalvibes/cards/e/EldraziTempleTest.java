package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AllIsDust;
import com.github.laxika.magicalvibes.cards.n.NestInvader;
import com.github.laxika.magicalvibes.cards.p.PropheticPrism;
import com.github.laxika.magicalvibes.cards.s.SpawnsireOfUlamog;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EldraziTemple.class, AllIsDust.class, NestInvader.class,
        PropheticPrism.class, SpawnsireOfUlamog.class})
class EldraziTempleTest extends BaseCardTest {

    @Test
    @DisplayName("The first ability adds one colorless mana")
    void firstAbilityAddsColorlessMana() {
        addReadyTemple();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("The second ability adds two colorless Eldrazi-restricted mana")
    void secondAbilityAddsRestrictedMana() {
        addReadyTemple();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId())
                .getColorlessSubtypeSpellOrAbilityMana(CardSubtype.ELDRAZI)).isEqualTo(2);
    }

    @Test
    @DisplayName("Restricted mana can cast a colorless Eldrazi spell")
    void restrictedManaCanCastColorlessEldraziSpell() {
        addReadyTemple();
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(colorlessEldrazi("Colorless Eldrazi", "{2}")));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId())
                .getColorlessSubtypeSpellOrAbilityMana(CardSubtype.ELDRAZI)).isZero();
    }

    @Test
    @DisplayName("Restricted mana cannot cast a colored Eldrazi spell")
    void restrictedManaCannotCastColoredEldraziSpell() {
        addReadyTemple();
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(coloredEldrazi("Colored Eldrazi", "{2}{G}")));

        harness.activateAbility(player1, 0, 1, null, null);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
        assertThat(gd.playerManaPools.get(player1.getId())
                .getColorlessSubtypeSpellOrAbilityMana(CardSubtype.ELDRAZI)).isEqualTo(2);
    }

    @Test
    @DisplayName("Restricted mana can pay for a colorless Eldrazi ability")
    void restrictedManaCanPayColorlessEldraziAbility() {
        addReadyTemple();
        Card eldrazi = colorlessEldrazi("Ability Eldrazi", "{2}");
        eldrazi.addActivatedAbility(new ActivatedAbility(
                false, "{1}", List.of(new GainLifeEffect(1)), "{1}: You gain 1 life."));
        harness.addToBattlefield(player1, eldrazi);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.activateAbility(player1, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId())
                .getColorlessSubtypeSpellOrAbilityMana(CardSubtype.ELDRAZI)).isEqualTo(1);
    }

    private void addReadyTemple() {
        harness.addToBattlefieldAndReturn(player1, new EldraziTemple()).setSummoningSick(false);
    }

    @Test
    void restrictedManaCastsRealColorlessEldraziCreature() {
        addReadyTemple();
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new SpawnsireOfUlamog()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId())
                .getColorlessSubtypeSpellOrAbilityMana(CardSubtype.ELDRAZI)).isZero();
    }

    @Test
    void restrictedManaCastsNoncreatureEldraziSpell() {
        addReadyTemple();
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new AllIsDust()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.castSorcery(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId())
                .getColorlessSubtypeSpellOrAbilityMana(CardSubtype.ELDRAZI)).isZero();
    }

    @Test
    void restrictedManaCannotPayGenericCostOfColoredEldrazi() {
        addReadyTemple();
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new NestInvader()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, 1, null, null);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId())
                .getColorlessSubtypeSpellOrAbilityMana(CardSubtype.ELDRAZI)).isEqualTo(2);
    }

    @Test
    void unrestrictedManaCanPayForColoredEldrazi() {
        addReadyTemple();
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new NestInvader()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    void restrictedManaCannotPayForNonEldraziAbility() {
        addReadyTemple();
        harness.addToBattlefieldAndReturn(player1, new PropheticPrism()).setSummoningSick(false);
        harness.activateAbility(player1, 0, 1, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId())
                .getColorlessSubtypeSpellOrAbilityMana(CardSubtype.ELDRAZI)).isEqualTo(2);
    }

    @Test
    void restrictedManaCannotCastColorlessNonEldraziSpell() {
        addReadyTemple();
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new PropheticPrism()));
        harness.activateAbility(player1, 0, 1, null, null);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId())
                .getColorlessSubtypeSpellOrAbilityMana(CardSubtype.ELDRAZI)).isEqualTo(2);
    }

    @Test
    void manaAbilitiesShareTheTapCost() {
        addReadyTemple();
        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId())
                .getColorlessSubtypeSpellOrAbilityMana(CardSubtype.ELDRAZI)).isEqualTo(2);
    }

    @Test
    void restrictedManaPaysForRealColorlessEldraziAbility() {
        addReadyTemple();
        harness.addToBattlefield(player1, new SpawnsireOfUlamog());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().isToken()).hasSize(2);
        assertThat(gd.playerManaPools.get(player1.getId())
                .getColorlessSubtypeSpellOrAbilityMana(CardSubtype.ELDRAZI)).isZero();
    }

    private static Card colorlessEldrazi(String name, String manaCost) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost(manaCost);
        card.setPower(2);
        card.setToughness(2);
        card.setSubtypes(List.of(CardSubtype.ELDRAZI));
        return card;
    }

    private static Card coloredEldrazi(String name, String manaCost) {
        Card card = colorlessEldrazi(name, manaCost);
        card.setColor(CardColor.GREEN);
        return card;
    }
}
