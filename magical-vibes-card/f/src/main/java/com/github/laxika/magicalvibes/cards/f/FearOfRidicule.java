package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ExileRandomCreatureCardFromDamagedPlayerLibraryAndCreateTokenCopyEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "YDSK", collectorNumber = "7")
public class FearOfRidicule extends Card {

    public FearOfRidicule() {
        PermanentPredicate enchantmentCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsEnchantmentPredicate(),
                new PermanentIsCreaturePredicate()
        ));

        addStaticEffect(new GrantKeywordEffect(
                Keyword.MENACE, GrantScope.ALL_OWN_CREATURES, enchantmentCreature));

        CreateTokenCopyOfTargetPermanentEffect tokenCopy = new CreateTokenCopyOfTargetPermanentEffect(
                List.of(), Set.of(CardType.ENCHANTMENT, CardType.CREATURE), 1, 1, java.util.Map.of(),
                false, false, false, false, false, false, null, Set.of()).withReplacedCardTypes();
        addTrigger(Triggers.creatureYouControlDealsCombatDamageToPlayer().withFilter(enchantmentCreature),
                TriggerOptions.ONE_OR_MORE,
                new ExileRandomCreatureCardFromDamagedPlayerLibraryAndCreateTokenCopyEffect(tokenCopy));
    }
}
