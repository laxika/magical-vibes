package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "WHO", collectorNumber = "188")
@CardRegistration(set = "WHO", collectorNumber = "462")
@CardRegistration(set = "WHO", collectorNumber = "793")
@CardRegistration(set = "WHO", collectorNumber = "1053")
public class GallifreyCouncilChamber extends Card {

    public GallifreyCouncilChamber() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new SurveilEffect(1));
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(AwardAnyColorManaEffect.forSpellOrAbilitySubtypes(
                        1, Set.of(CardSubtype.TIME_LORD, CardSubtype.ALIEN))),
                "{T}: Add one mana of any color. Spend this mana only to cast a Time Lord or Alien spell or activate an ability of a Time Lord or Alien."
        ));
    }
}
