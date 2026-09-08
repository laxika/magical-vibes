package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CrackdownConstructTest extends BaseCardTest {

    @Test
    @DisplayName("Activating artifact and creature abilities gives +1/+1 for each activation")
    void artifactAndCreatureAbilitiesBoostConstruct() {
        Permanent construct = addCreatureReady(player1, new CrackdownConstruct());
        addPermanentWithAbility(player1, CardType.ARTIFACT, new BoostSelfEffect(1, 0));
        addPermanentWithAbility(player1, CardType.CREATURE, new BoostSelfEffect(1, 0));

        harness.activateAbility(player1, 1, null, null);
        resolveAllTriggers();
        harness.activateAbility(player1, 2, null, null);
        resolveAllTriggers();

        assertThat(construct.getPowerModifier()).isEqualTo(2);
        assertThat(construct.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Activating a mana ability does not boost Construct")
    void manaAbilityDoesNotBoostConstruct() {
        Permanent construct = addCreatureReady(player1, new CrackdownConstruct());
        addPermanentWithAbility(player1, CardType.ARTIFACT, new AwardManaEffect(ManaColor.GREEN));

        harness.activateAbility(player1, 1, null, null);

        assertThat(gd.stack).isEmpty();
        assertThat(construct.getPowerModifier()).isZero();
        assertThat(construct.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Activating an enchantment ability does not boost Construct")
    void nonArtifactNonCreatureAbilityDoesNotBoostConstruct() {
        Permanent construct = addCreatureReady(player1, new CrackdownConstruct());
        addPermanentWithAbility(player1, CardType.ENCHANTMENT, new BoostSelfEffect(1, 0));

        harness.activateAbility(player1, 1, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(construct.getPowerModifier()).isZero();
        assertThat(construct.getToughnessModifier()).isZero();
    }

    private void addPermanentWithAbility(Player player, CardType type, CardEffect effect) {
        Card card = new Card();
        card.setName("Ability Source");
        card.setType(type);
        if (type == CardType.CREATURE) {
            card.setPower(1);
            card.setToughness(1);
        }
        card.addActivatedAbility(new ActivatedAbility(true, null, List.of(effect), "{T}: ability."));
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
    }
}
