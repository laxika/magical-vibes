package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.CreateTokenAttachedToTargetEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.TotemArmorEffect;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "SLD", collectorNumber = "1327")
public class EstridTheMasked extends Card {

    public EstridTheMasked() {
        addActivatedAbility(new ActivatedAbility(
                +2,
                List.of(new UntapPermanentsEffect(
                        TapUntapScope.CONTROLLED, new PermanentIsEnchantedPredicate())),
                "+2: Untap each enchanted permanent you control."
        ));

        PermanentNotPredicate anotherPermanent = new PermanentNotPredicate(
                new PermanentIsSourcePermanentPredicate());
        addActivatedAbility(new ActivatedAbility(
                -1,
                List.of(new CreateTokenAttachedToTargetEffect(
                        maskToken(), new PermanentTruePredicate())),
                "−1: Create a white Aura enchantment token named Mask attached to another target permanent. "
                        + "The token has enchant permanent and umbra armor.",
                new PermanentPredicateTargetFilter(anotherPermanent, "Target must be another permanent")
        ));

        CardAllOfPredicate nonAuraEnchantment = new CardAllOfPredicate(List.of(
                new CardTypePredicate(CardType.ENCHANTMENT),
                new CardNotPredicate(new CardSubtypePredicate(CardSubtype.AURA))));
        CardAllOfPredicate auraEnchantment = new CardAllOfPredicate(List.of(
                new CardTypePredicate(CardType.ENCHANTMENT),
                new CardSubtypePredicate(CardSubtype.AURA)));
        addActivatedAbility(new ActivatedAbility(
                -7,
                List.of(
                        new MillEffect(7, MillRecipient.CONTROLLER),
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                                .filter(nonAuraEnchantment)
                                .returnAll(true)
                                .build(),
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                                .filter(auraEnchantment)
                                .attachmentTarget(new PermanentTruePredicate())
                                .returnAll(true)
                                .build()),
                "−7: Mill seven cards. Return all non-Aura enchantment cards from your graveyard to the battlefield, "
                        + "then do the same for Aura cards."
        ));
    }

    private static CreateTokenEffect maskToken() {
        return new CreateTokenEffect(
                CardType.ENCHANTMENT,
                1,
                "Mask",
                0,
                0,
                CardColor.WHITE,
                null,
                List.of(CardSubtype.AURA),
                Set.of(),
                Set.of(),
                false,
                false,
                Map.of(EffectSlot.STATIC, new TotemArmorEffect()),
                List.of(),
                false,
                false,
                false,
                0,
                Set.of())
                .withTokenTargetFilter(TargetFilters.permanent());
    }
}
