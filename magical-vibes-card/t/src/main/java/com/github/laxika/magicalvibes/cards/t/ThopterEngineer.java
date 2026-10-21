package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ORI", collectorNumber = "165")
@CardRegistration(set = "2XM", collectorNumber = "147")
@CardRegistration(set = "PIO", collectorNumber = "344")
@CardRegistration(set = "C21", collectorNumber = "181")
@CardRegistration(set = "BLC", collectorNumber = "204")
@CardRegistration(set = "C18", collectorNumber = "126")
public class ThopterEngineer extends Card {

    public ThopterEngineer() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CreateTokenEffect(
                1,
                "Thopter", 1, 1, null, List.of(CardSubtype.THOPTER),
                Set.of(Keyword.FLYING), Set.of(CardType.ARTIFACT)));

        // Haste for artifact creatures you control, including Thopter Engineer itself when it is an
        // artifact (e.g. from Liquimetal Coating).
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(
                Keyword.HASTE,
                GrantScope.ALL_OWN_CREATURES,
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsArtifactPredicate(),
                        new PermanentIsCreaturePredicate()))));
    }
}
