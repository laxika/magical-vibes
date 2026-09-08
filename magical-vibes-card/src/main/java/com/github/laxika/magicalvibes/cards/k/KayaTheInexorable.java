package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateEmblemEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EmblemStepTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.EmblemTriggerStep;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToTargetEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastLegendarySpellFromAnyZoneEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromExileToOwnerHandEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToOwnerHandEffect;
import com.github.laxika.magicalvibes.model.effect.SelfExiledFromBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import java.util.List;
import java.util.Set;

@CardRegistration(set = "KHM", collectorNumber = "218")
public class KayaTheInexorable extends Card {

    private static final String EMBLEM_TEXT =
            "At the beginning of your upkeep, you may cast a legendary spell from your hand, "
                    + "from your graveyard, or from among cards you own in exile without paying its mana cost.";
    private static final CardPredicate LEGENDARY_SPELL = new CardAllOfPredicate(List.of(
            new CardSupertypePredicate(CardSupertype.LEGENDARY),
            new CardNotPredicate(new CardTypePredicate(CardType.LAND))));

    public KayaTheInexorable() {
        PermanentAllOfPredicate nontokenCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentIsTokenPredicate())));
        SequenceEffect returnFromGraveyard = SequenceEffect.of(
                new ReturnSourceCardFromGraveyardToOwnerHandEffect(),
                new CreateTokenEffect("Spirit", 1, 1, CardColor.WHITE,
                        List.of(CardSubtype.SPIRIT), Set.of(Keyword.FLYING), Set.of()));
        SequenceEffect returnFromExile = SequenceEffect.of(
                new ReturnSourceCardFromExileToOwnerHandEffect(),
                new CreateTokenEffect("Spirit", 1, 1, CardColor.WHITE,
                        List.of(CardSubtype.SPIRIT), Set.of(Keyword.FLYING), Set.of()));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new PutCounterOnTargetPermanentEffect(CounterType.GHOSTFORM),
                        new GrantEffectToTargetEffect(
                                EffectSlot.ON_DEATH, returnFromGraveyard, EffectDuration.PERMANENT, false),
                        new GrantEffectToTargetEffect(
                                EffectSlot.ON_SELF_LEAVES_BATTLEFIELD,
                                new SelfExiledFromBattlefieldEffect(returnFromExile),
                                EffectDuration.PERMANENT,
                                false)),
                "+1: Put a ghostform counter on up to one target nontoken creature. It gains \"When this creature dies "
                        + "or is put into exile, return it to its owner's hand and create a 1/1 white Spirit creature "
                        + "token with flying.\"",
                new PermanentPredicateTargetFilter(nontokenCreature, "Target must be a nontoken creature"),
                +1,
                null,
                null,
                List.of(),
                0,
                1
        ));

        addActivatedAbility(new ActivatedAbility(
                -3,
                List.of(new ExileTargetPermanentEffect()),
                "-3: Exile target nonland permanent.",
                TargetFilters.nonlandPermanent()
        ));

        addActivatedAbility(new ActivatedAbility(
                -7,
                List.of(new CreateEmblemEffect(
                        List.of(new EmblemStepTriggerEffect(
                                EmblemTriggerStep.UPKEEP,
                                List.of(new MayCastLegendarySpellFromAnyZoneEffect(LEGENDARY_SPELL)),
                                EMBLEM_TEXT)),
                        EMBLEM_TEXT)),
                "-7: You get an emblem with \"" + EMBLEM_TEXT + "\"."
        ));
    }
}
