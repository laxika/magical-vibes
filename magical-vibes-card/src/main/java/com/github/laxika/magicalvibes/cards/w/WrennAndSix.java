package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CreateEmblemEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantSpellCastingAbilityToSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "MH1", collectorNumber = "217")
public class WrennAndSix extends Card {

    public WrennAndSix() {
        // +1: Return up to one target land card from your graveyard to your hand.
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.HAND)
                        .filter(new CardTypePredicate(CardType.LAND))
                        .targetGraveyard(true)
                        .upTo(true)
                        .build()),
                "+1: Return up to one target land card from your graveyard to your hand."
        ));

        // −1: Wrenn and Six deals 1 damage to any target.
        addActivatedAbility(new ActivatedAbility(
                -1,
                List.of(new DealDamageToAnyTargetEffect(1)),
                "−1: Wrenn and Six deals 1 damage to any target."
        ));

        // −7: You get an emblem with "Instant and sorcery cards in your graveyard have retrace."
        addActivatedAbility(new ActivatedAbility(
                -7,
                List.of(new CreateEmblemEffect(
                        List.of(GrantSpellCastingAbilityToSpellsEffect.fromZone(
                                Keyword.RETRACE,
                                new CardAnyOfPredicate(List.of(
                                        new CardTypePredicate(CardType.INSTANT),
                                        new CardTypePredicate(CardType.SORCERY))),
                                Zone.GRAVEYARD)),
                        "Instant and sorcery cards in your graveyard have retrace.")),
                "−7: You get an emblem with \"Instant and sorcery cards in your graveyard have retrace.\""
        ));
    }
}
